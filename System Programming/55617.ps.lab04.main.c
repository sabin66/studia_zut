//PS IS1 322 LAB04
//Dorian Sobierański
//sd55617@zut.edu.pl
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <time.h>
#include <fcntl.h>
#include <string.h>

int main(int argc, char *argv[]){
    int vinfo = 0;
    int runs = 1;
    int opt;
    while ( (opt = getopt(argc, argv, "+vt:")) != -1){
        switch(opt){
            case 'v':
                vinfo = 1;
                break;
            case 't':
                runs = atoi(optarg);
                if( runs <= 0){
                    fprintf(stderr,"Liczba uruchomien musi byc wieksza niz 0");
                    exit(EXIT_FAILURE);
                }
                break;
            default:
                fprintf(stderr,"Usage %s [-v] [-t liczba_powtorzen] program [argumenty...]\n",argv[0]);
                exit(EXIT_FAILURE);
        }
    }
    if (optind >= argc){ // optind = index of the next element to be processed in argv
        fprintf(stderr, "Błąd: Nie podano programu testowego.\n");
        fprintf(stderr, "Użycie: %s [-v] [-t liczba_powtórzeń] program [argumenty...]\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    // gdy getopt() skonczy i trafi na cos, co nie jest opcją zatrzymuje się -> optind przechowuje indeks pierwszego elementu, który nie był opcją.
    double real = 0.0 ;
    double user = 0.0;
    double sys = 0.0;
    for (int i = 0; i < runs; i++){
        struct timespec start,end;
        clock_gettime(CLOCK_MONOTONIC, &start);
        pid_t pid = fork();
        if (pid < 0){
            exit(EXIT_FAILURE);
        }
        else if(pid == 0){
            if(!vinfo){
                close(1);
                close(2);
                int h = open("/dev/null", O_WRONLY);
                dup2(h, 1);
                dup2(h, 2);
                close(h);
            }
            execvp(argv[optind],&argv[optind]); // funkcja do zastapienia biezacego obrazu procesu nowym programem, argv[optind] = np. find
            exit(EXIT_FAILURE);
        }
        else{
            int status;
            struct rusage usage;

            if(wait4(pid,&status,0,&usage)== -1){
                exit(EXIT_FAILURE);
            }

            clock_gettime(CLOCK_MONOTONIC, &end);

            double real_t = (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec)/1e9;
            double user_t = usage.ru_utime.tv_sec + usage.ru_utime.tv_usec / 1e6;
            double sys_t  = usage.ru_stime.tv_sec + usage.ru_stime.tv_usec / 1e6;

            real += real_t;
            user += user_t;
            sys += sys_t;

            printf("### Pomiar %d ###\n", i + 1);
            printf("  Real:   %.5f s\n", real_t);
            printf("  User:   %.5f s\n", user_t);
            printf("  System: %.5f s\n", sys_t);
        }
    }
    if (runs > 1) {
        printf("\n### PODSUMOWANIE (średnia z %d uruchomień) ###\n", runs);
        printf("  Real:   %.5f s\n", real / runs);
        printf("  User:   %.5f s\n", user / runs);
        printf("  System: %.5f s\n", sys / runs);
    }

    return 0;
}