//PS IS1 322 LAB05
//Dorian Sobierański
//sd55617@zut.edu.pl
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <time.h>

// https://en.cppreference.com/w/c/program/sig_atomic_t.html
volatile sig_atomic_t skip = 0;
volatile sig_atomic_t fail = 0;

void handle_sigusr1(int sig) { skip = 1; }
void handle_sigusr2(int sig) { fail = 1; }

int main(int argc, char *argv[]){
    if (argc!= 2){
        fprintf(stderr,"Usage: %s <H_interval>\n", argv[0]);
        return 1;
    }

    int h = atoi(argv[1]);
    if (h <= 0){
        h = 1; 
    }

    struct sigaction sa1 = {0};
    sa1.sa_handler = handle_sigusr1;
    sigemptyset(&sa1.sa_mask);
    sigaction(SIGUSR1,&sa1, NULL);

    struct sigaction sa2 = {0};
    sa2.sa_handler = handle_sigusr2;
    sigemptyset(&sa2.sa_mask);
    sigaction(SIGUSR2,&sa2, NULL);

    int seq = 0;
    pid_t pid = getpid();
    pid_t ppid = getppid(); // watchdog  - parent process

    while(1){
        sleep(h);
        seq++;
        time_t now = time(NULL);

        if(skip){
            printf("[LOCAL] PID=%d seq=%d type=FAILURE_SKIP\n",pid,seq);
            skip = 0;
            continue;
        }
        int type = 0;
        if(fail){
            type = 1;
            printf("[LOCAL] PID=%d seq=%d type=FAILURE_SOFT\n",pid,seq);
            fail=0;
        }else{
            printf("[LOCAL] PID=%d seq=%d type=HEARTBEAT\n",pid,seq);
        }

        union sigval value;
        value.sival_int = (seq << 8) | type; //od geminiego

        sigqueue(ppid,SIGRTMIN,value);
    }
    return 0;


}