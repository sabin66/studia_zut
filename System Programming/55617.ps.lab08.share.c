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
#include <time.h>

#define MAX_WORD 128
#define MAX_NAME 64

// Struktura kontrolna na poczatku pamieci wspoldzielonej
struct control {
    int total_tasks;
    int num_words;
    int found;       // 0=nie znaleziono, 1=znaleziono, -1=stop
    char found_password[MAX_WORD];
    char hash[MAX_WORD];
};

// Komunikat opisujacy podzadanie
struct task_msg {
    int task_id;
    int start_idx;   // poczatek zakresu slow (wlacznie)
    int end_idx;     // koniec zakresu slow (wylacznie)
    char shm_name[MAX_NAME];
    char sem_name[MAX_NAME];
};

// Globalne zmienne do czyszczenia
static char g_mq_name[MAX_NAME];
static char g_shm_name[MAX_NAME];
static char g_sem_name[MAX_NAME];
static mqd_t g_mq = (mqd_t)-1;
static int g_shm_fd = -1;
static void *g_shm_ptr = NULL;
static size_t g_shm_size = 0;
static sem_t *g_sem = SEM_FAILED;
static volatile sig_atomic_t g_quit = 0;

void cleanup(void) {
    if (g_shm_ptr != NULL && g_shm_ptr != MAP_FAILED)
        munmap(g_shm_ptr, g_shm_size);
    if (g_shm_fd >= 0) close(g_shm_fd);
    if (g_mq != (mqd_t)-1) mq_close(g_mq);
    if (g_sem != SEM_FAILED) sem_close(g_sem);
    shm_unlink(g_shm_name);
    mq_unlink(g_mq_name);
    sem_unlink(g_sem_name);
}

void sig_handler(int sig) {
    (void)sig;
    g_quit = 1;
}

// Offsety w pamieci wspoldzielonej
// [struct control] [int status[total_tasks]] [char words[num_words][MAX_WORD]]
static int *get_task_status(void *shm) {
    return (int *)((char *)shm + sizeof(struct control));
}

static char *get_words_area(void *shm, int total_tasks) {
    return (char *)shm + sizeof(struct control) + total_tasks * sizeof(int);
}

int main(int argc, char *argv[]) {
    if (argc != 4) {
        fprintf(stderr, "Uzycie: %s <plik_slownika> <skrot_hasla> <liczba_podzadan>\n", argv[0]);
        return 1;
    }

    const char *dict_file = argv[1];
    const char *hash = argv[2];
    int num_tasks = atoi(argv[3]);

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

    // Wygeneruj nazwy IPC
    pid_t pid = getpid();
    snprintf(g_mq_name,  MAX_NAME, "/cr_mq_%d",  pid);
    snprintf(g_shm_name, MAX_NAME, "/cr_shm_%d", pid);
    snprintf(g_sem_name, MAX_NAME, "/cr_sem_%d",  pid);

    atexit(cleanup);

    // Wczytaj slownik
    FILE *f = fopen(dict_file, "r");
    if (!f) { perror("fopen"); return 1; }

    char **words = NULL;
    int num_words = 0;
    int cap = 4096;
    words = malloc(cap * sizeof(char *));
    if (!words) { perror("malloc"); fclose(f); return 1; }

    char line[MAX_WORD];
    while (fgets(line, sizeof(line), f)) {
        line[strcspn(line, "\r\n")] = 0;
        if (strlen(line) == 0) continue;
        if (num_words >= cap) {
            cap *= 2;
            words = realloc(words, cap * sizeof(char *));
        }
        words[num_words] = strdup(line);
        num_words++;
    }
    fclose(f);

    printf("Wczytano %d slow ze slownika\n", num_words);

    if (num_tasks > num_words) num_tasks = num_words;

    // Oblicz rozmiar pamieci wspoldzielonej
    g_shm_size = sizeof(struct control)
               + (size_t)num_tasks * sizeof(int)
               + (size_t)num_words * MAX_WORD;

    // Utworz pamiec wspoldzielona
    g_shm_fd = shm_open(g_shm_name, O_CREAT | O_RDWR, 0666);
    if (g_shm_fd < 0) { perror("shm_open"); return 1; }
    if (ftruncate(g_shm_fd, g_shm_size) < 0) { perror("ftruncate"); return 1; }

    g_shm_ptr = mmap(NULL, g_shm_size, PROT_READ | PROT_WRITE, MAP_SHARED, g_shm_fd, 0);
    if (g_shm_ptr == MAP_FAILED) { perror("mmap"); return 1; }

    // Wypelnij strukture kontrolna
    struct control *ctrl = (struct control *)g_shm_ptr;
    ctrl->total_tasks = num_tasks;
    ctrl->num_words = num_words;
    ctrl->found = 0;
    memset(ctrl->found_password, 0, MAX_WORD);
    strncpy(ctrl->hash, hash, MAX_WORD - 1);
    ctrl->hash[MAX_WORD - 1] = 0;

    // Statusy zadan: 0=oczekuje, 1=w trakcie, 2=zakonczone
    int *task_status = get_task_status(g_shm_ptr);
    memset(task_status, 0, num_tasks * sizeof(int));

    // Skopiuj slowa do pamieci wspoldzielonej
    char *words_area = get_words_area(g_shm_ptr, num_tasks);
    for (int i = 0; i < num_words; i++) {
        memset(words_area + (size_t)i * MAX_WORD, 0, MAX_WORD);
        strncpy(words_area + (size_t)i * MAX_WORD, words[i], MAX_WORD - 1);
        free(words[i]);
    }
    free(words);

    // Utworz semafor
    g_sem = sem_open(g_sem_name, O_CREAT, 0666, 1);
    if (g_sem == SEM_FAILED) { perror("sem_open"); return 1; }

    // Utworz kolejke komunikatow
    struct mq_attr attr;
    attr.mq_flags = 0;
    attr.mq_maxmsg = 10;
    attr.mq_msgsize = sizeof(struct task_msg);
    attr.mq_curmsgs = 0;

    g_mq = mq_open(g_mq_name, O_CREAT | O_WRONLY, 0666, &attr);
    if (g_mq == (mqd_t)-1) { perror("mq_open"); return 1; }

    printf("=== Kolejka komunikatow: %s ===\n", g_mq_name);
    printf("Pamiec wspoldzielona: %s\n", g_shm_name);
    printf("Semafor: %s\n", g_sem_name);
    printf("Skrot do zlamania: %s\n", hash);
    printf("Liczba podzadan: %d\n", num_tasks);

    // Podziel slownik na podzadania i wyslij komunikaty
    int words_per_task = num_words / num_tasks;
    int extra = num_words % num_tasks;
    int current = 0;

    for (int i = 0; i < num_tasks && !g_quit; i++) {
        struct task_msg msg;
        memset(&msg, 0, sizeof(msg));
        msg.task_id = i;
        msg.start_idx = current;
        int count = words_per_task + (i < extra ? 1 : 0);
        msg.end_idx = current + count;
        current = msg.end_idx;
        strncpy(msg.shm_name, g_shm_name, MAX_NAME - 1);
        strncpy(msg.sem_name, g_sem_name, MAX_NAME - 1);

        // mq_send blokuje gdy kolejka pelna
        while (mq_send(g_mq, (char *)&msg, sizeof(msg), 0) < 0) {
            if (errno == EINTR) {
                if (g_quit) break;
                continue;
            }
            perror("mq_send");
            return 1;
        }
        printf("Wyslano podzadanie %d: slowa [%d, %d)\n", i, msg.start_idx, msg.end_idx);
    }

    if (g_quit) {
        printf("\nPrzerwano podczas konfiguracji.\n");
        return 1;
    }

    printf("\nWszystkie podzadania w kolejce. Oczekiwanie na workery...\n\n");

    // Monitoruj postep obliczen
    while (!g_quit) {
        if (sem_wait(g_sem) < 0) {
            if (errno == EINTR) continue;
            break;
        }

        int waiting = 0, in_progress = 0, completed = 0;
        for (int i = 0; i < num_tasks; i++) {
            switch (task_status[i]) {
                case 0: waiting++; break;
                case 1: in_progress++; break;
                case 2: completed++; break;
            }
        }
        int found = ctrl->found;
        char password[MAX_WORD] = {0};
        if (found == 1)
            strncpy(password, ctrl->found_password, MAX_WORD - 1);

        sem_post(g_sem);

        printf("\rPostep: oczekuje=%d  w_trakcie=%d  zakonczone=%d/%d   ",
               waiting, in_progress, completed, num_tasks);
        fflush(stdout);

        if (found == 1) {
            printf("\n\n*** HASLO ZNALEZIONE: %s ***\n", password);
            // Ustaw flage stop zeby workery przestaly
            sem_wait(g_sem);
            ctrl->found = 1;
            sem_post(g_sem);
            break;
        }

        if (completed == num_tasks) {
            printf("\n\nWszystkie podzadania zakonczone. Haslo NIE znalezione.\n");
            break;
        }

        usleep(300000);
    }

    if (g_quit) {
        printf("\n\nPrzerwano (SIGINT). Czyszczenie zasobow...\n");
        // Powiadom workery o zakonczeniu
        sem_wait(g_sem);
        ctrl->found = -1;
        sem_post(g_sem);
    }

    // cleanup() zostanie wywolane przez atexit
    return 0;
}
