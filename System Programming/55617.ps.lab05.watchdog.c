//PS IS1 322 LAB05
//Dorian Sobierański
//sd55617@zut.edu.pl
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <time.h>
#include <sys/wait.h>
#include <string.h>

#define MAX_WORKERS 100

typedef struct {
    pid_t pid;
    int last_seq;
    time_t last_hb;
    int active;
} WorkerState;

int num_workers = 0;
WorkerState workers[MAX_WORKERS];
volatile sig_atomic_t keep_running = 1;

void handle_sigint(int sig){ keep_running = 0;}
void handle_rt(int sig, siginfo_t *info, void *context){
    pid_t sender_pid = info->si_pid;
    int val = info->si_value.sival_int;
    
    int type = val & 0xFF;
    int seq = val >> 8;

    // funkcja poprawiona z pomoca Artur Mizuła
    for (int i = 0; i < num_workers; i++) {
        if (workers[i].pid == sender_pid && workers[i].active) {
            if(type == 0){
                printf("[RECV] PID=%d seq=%d type=HEARTBEAT\n",sender_pid,seq);
            }else if (type == 1){
                printf("[RECV] PID=%d seq=%d type=FAILURE_SOFT\n",sender_pid,seq);
            }
            if (workers[i].last_seq != -1 && seq != workers[i].last_seq + 1) {
                printf("[WARNING] PID=%d missing seq: %d\n", sender_pid, workers[i].last_seq + 1);
            }
            workers[i].last_seq = seq;
            workers[i].last_hb = time(NULL);
            break;
        }
    }
}
pid_t spawn_worker(int h){
    pid_t pid = fork();
    if (pid == 0){
        char h_str[32];
        snprintf(h_str, sizeof(h_str), "%d", h);
        char *cmd[] = {"./worker",h_str,NULL};
        execvp(cmd[0],cmd);
        perror("execvp failed");
        exit(1);
    }
    return pid;
}

int main(int argc, char *argv[]){
    int N = 0;
    int H = 0;
    int T = 0;
    int opt;
    while ((opt = getopt(argc, argv, "n:h:t:")) != -1) {
        switch (opt) {
            case 'n':
                N = atoi(optarg);
                break;
            case 'h':
                H = atoi(optarg);
                break;
            case 't':
                T = atoi(optarg);
                break;
            default:
                fprintf(stderr, "Usage: %s -n <N> -h <H> -t <T>\n", argv[0]);
                return 1;
        }
    }

    num_workers = N;
    struct sigaction sa_int = {0};
    sa_int.sa_handler = handle_sigint;
    sigemptyset(&sa_int.sa_mask);
    sigaction(SIGINT, &sa_int, NULL);

    struct sigaction sa_rt = {0};
    sa_rt.sa_sigaction = handle_rt;
    sa_rt.sa_flags = SA_SIGINFO | SA_RESTART; // SA_SIGINFO - umozliwia czytanie payload; wypelnia siginfo_t
    sigemptyset(&sa_rt.sa_mask);
    sigaction(SIGRTMIN, &sa_rt, NULL);

    for (int i = 0; i < N; i++) {
        workers[i].pid = spawn_worker(H);
        workers[i].last_seq = -1;
        workers[i].last_hb = time(NULL);
        workers[i].active = 1;
    }

    while (keep_running) {
        sleep(1);
        time_t now = time(NULL);

        for (int i = 0; i < N; i++) {
            if (workers[i].active && (now - workers[i].last_hb > T)) {
                printf("[WATCHDOG] PID=%d timeout! Restarting...\n", workers[i].pid);
                
                kill(workers[i].pid, SIGKILL);
                waitpid(workers[i].pid, NULL, 0);

                workers[i].pid = spawn_worker(H);
                workers[i].last_seq = -1;
                workers[i].last_hb = time(NULL);
            }
        }
    }

    printf("\n[WATCHDOG] Shutting down. Killing workers...\n");
    for (int i = 0; i < N; i++) {
        if (workers[i].active) {
            kill(workers[i].pid, SIGKILL);
            waitpid(workers[i].pid, NULL, 0);
        }
    }

    return 0;
}