//PS IS1 322 LAB02
//Dorian Sobierański
//sd55617@zut.edu.pl
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <utmpx.h>
#include <unistd.h>

char** getUserGroups(const char* user, int* num_groups);

int main(int argc, char *argv[]){
    int show_groups = 0;
    int opt;
    while ( (opt = getopt(argc, argv, "g")) != -1){
        switch(opt){
            case 'g':
                show_groups = 1;
                break;
            default:
                fprintf(stderr,"UsageL %s [-g]\n",argv[0]);
                exit(EXIT_FAILURE);
        }
    }
    struct utmpx *ut;
    setutxent();

    while ((ut = getutxent()) != NULL){
        if (ut->ut_type == USER_PROCESS){
            if(!show_groups){
                printf("%s\n", ut->ut_user);
            }else{
                int n_groups = 0;
                char **groups = getUserGroups(ut->ut_user,&n_groups);

                if(groups != NULL){
                    printf("%s[", ut->ut_user);
                    for(int i = 0; i<n_groups; i++){
                        printf("%s%s", groups[i],(i < n_groups -1)? ", " : "");
                        free(groups[i]);
                    }
                    printf("]\n");
                    free(groups);
                }else{
                printf("%s[error]\n",ut->ut_user);
            }
            }
        }
    }
    endutxent();
    return 0;
}