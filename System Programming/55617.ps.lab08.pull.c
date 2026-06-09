// PS IS1 322 LAB08
// Dorian Sobieranski
// sd55617@zut.edu.pl
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <mqueue.h>
#include <semaphore.h>
#include <signal.h>
#include <errno.h>
#include <crypt.h>

#define MAX_WORD 128
#define MAX_NAME 64

// Musi byc identyczna jak w share.c
struct control {
    int total_tasks;
    int num_words;
    int found;
    char found_password[MAX_WORD];
    char hash[MAX_WORD];
};

struct task_msg {
    int task_id;
    int start_idx;
    int end_idx;
    char shm_name[MAX_NAME];
    char sem_name[MAX_NAME];
};

// Globalne - potrzebne w obsludze sygnalu
static volatile sig_atomic_t g_quit = 0;
static mqd_t g_mq = (mqd_t)-1;
static struct task_msg g_current_task;
static int g_has_task = 0;
static void *g_shm_ptr = NULL;
static size_t g_shm_size = 0;
static sem_t *g_sem = SEM_FAILED;
static int g_shm_fd = -1;

void sig_handler(int sig) {
    (void)sig;
    g_quit = 1;
}

static int *get_task_status(void *shm) {
    return (int *)((char *)shm + sizeof(struct control));
}

static char *get_words_area(void *shm, int total_tasks) {
    return (char *)shm + sizeof(struct control) + total_tasks * sizeof(int);
}

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Uzycie: %s <nazwa_kolejki> <liczba_podzadan>\n", argv[0]);
        return 1;
    }

    const char *mq_name = argv[1];
    int num_tasks = atoi(argv[2]);

    if (num_tasks <= 0) {
        fprintf(stderr, "Liczba podzadan musi byc > 0\n");
        return 1;
    }

    // Obsluga sygnalow
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = sig_handler;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGINT, &sa, NULL);

    // Otworz kolejke komunikatow (RDWR - zeby moc oddac zadanie)
    g_mq = mq_open(mq_name, O_RDWR);
    if (g_mq == (mqd_t)-1) { perror("mq_open"); return 1; }

    // Pobierz rozmiar komunikatu z atrybutow kolejki
    struct mq_attr attr;
    mq_getattr(g_mq, &attr);
    size_t msg_buf_size = attr.mq_msgsize;
    char *msg_buf = malloc(msg_buf_size);
    if (!msg_buf) { perror("malloc"); return 1; }

    printf("Worker PID=%d: Start, planuje wykonac %d podzadan\n", getpid(), num_tasks);

    for (int t = 0; t < num_tasks && !g_quit; t++) {
        // Pobierz podzadanie z kolejki
        unsigned int prio;
        ssize_t n;
        while ((n = mq_receive(g_mq, msg_buf, msg_buf_size, &prio)) < 0) {
            if (errno == EINTR) {
                if (g_quit) break;
                continue;
            }
            perror("mq_receive");
            goto done;
        }
        if (g_quit) break;

        struct task_msg msg;
        memcpy(&msg, msg_buf, sizeof(msg));
        g_current_task = msg;
        g_has_task = 1;

        printf("Worker %d: Pobrano podzadanie %d (slowa %d-%d)\n",
               getpid(), msg.task_id, msg.start_idx, msg.end_idx);

        // Otworz pamiec wspoldzielona (przy pierwszym zadaniu)
        if (g_shm_ptr == NULL) {
            g_shm_fd = shm_open(msg.shm_name, O_RDWR, 0);
            if (g_shm_fd < 0) { perror("shm_open"); goto done; }

            // Odczytaj rozmiary z kontrolnej struktury
            struct control tmp;
            if (pread(g_shm_fd, &tmp, sizeof(tmp), 0) != sizeof(tmp)) {
                perror("pread");
                goto done;
            }

            g_shm_size = sizeof(struct control)
                       + (size_t)tmp.total_tasks * sizeof(int)
                       + (size_t)tmp.num_words * MAX_WORD;

            g_shm_ptr = mmap(NULL, g_shm_size, PROT_READ | PROT_WRITE,
                             MAP_SHARED, g_shm_fd, 0);
            if (g_shm_ptr == MAP_FAILED) {
                perror("mmap");
                g_shm_ptr = NULL;
                goto done;
            }

            g_sem = sem_open(msg.sem_name, 0);
            if (g_sem == SEM_FAILED) { perror("sem_open"); goto done; }
        }

        struct control *ctrl = (struct control *)g_shm_ptr;
        int *task_status = get_task_status(g_shm_ptr);
        char *words_area = get_words_area(g_shm_ptr, ctrl->total_tasks);

        // Oznacz zadanie jako "w trakcie"
        sem_wait(g_sem);
        task_status[msg.task_id] = 1;
        sem_post(g_sem);

        // Sprawdz czy juz znaleziono haslo
        if (ctrl->found != 0) {
            sem_wait(g_sem);
            task_status[msg.task_id] = 2;
            sem_post(g_sem);
            g_has_task = 0;
            printf("Worker %d: Haslo juz znalezione/stop, pomijam zadanie %d\n",
                   getpid(), msg.task_id);
            break;
        }

        // Odczytaj hash
        char hash[MAX_WORD];
        strncpy(hash, ctrl->hash, MAX_WORD - 1);
        hash[MAX_WORD - 1] = 0;

        // Przetwarzaj slowa
        int found = 0;
        for (int i = msg.start_idx; i < msg.end_idx && !g_quit; i++) {
            char *word = words_area + (size_t)i * MAX_WORD;

            char *result = crypt(word, hash);
            if (result && strcmp(result, hash) == 0) {
                // Znaleziono haslo!
                sem_wait(g_sem);
                ctrl->found = 1;
                strncpy(ctrl->found_password, word, MAX_WORD - 1);
                ctrl->found_password[MAX_WORD - 1] = 0;
                sem_post(g_sem);

                printf("Worker %d: ZNALEZIONO haslo: %s\n", getpid(), word);
                found = 1;
                break;
            }

            // Co jakis czas sprawdz czy ktos inny nie znalazl
            if ((i - msg.start_idx) % 100 == 99) {
                if (ctrl->found != 0) {
                    printf("Worker %d: Inny worker znalazl haslo\n", getpid());
                    found = 1;
                    break;
                }
            }
        }

        // Oznacz zadanie jako zakonczone
        sem_wait(g_sem);
        task_status[msg.task_id] = 2;
        sem_post(g_sem);

        g_has_task = 0;
        printf("Worker %d: Zakonczono podzadanie %d\n", getpid(), msg.task_id);

        if (found || ctrl->found != 0) break;
    }

done:
    // Jesli przerwano w trakcie zadania - oddaj je do kolejki
    if (g_quit && g_has_task) {
        printf("\nWorker %d: Przerwano, zwracam podzadanie %d do kolejki\n",
               getpid(), g_current_task.task_id);

        if (g_shm_ptr && g_sem != SEM_FAILED) {
            int *task_status = get_task_status(g_shm_ptr);
            sem_wait(g_sem);
            task_status[g_current_task.task_id] = 0;
            sem_post(g_sem);
        }

        // Oddaj komunikat z powrotem do kolejki
        mq_send(g_mq, (char *)&g_current_task, sizeof(g_current_task), 0);
    }

    // Posprzataj (NIE usuwaj obiektow IPC - to robi share)
    free(msg_buf);
    if (g_shm_ptr && g_shm_ptr != MAP_FAILED) munmap(g_shm_ptr, g_shm_size);
    if (g_shm_fd >= 0) close(g_shm_fd);
    if (g_sem != SEM_FAILED) sem_close(g_sem);
    if (g_mq != (mqd_t)-1) mq_close(g_mq);

    printf("Worker %d: Koniec\n", getpid());
    return 0;
}
