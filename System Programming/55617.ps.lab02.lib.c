//PS IS1 322 LAB02
//Dorian Sobierański
//sd55617@zut.edu.pl
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pwd.h>
#include <grp.h>
#include <utmpx.h>
#include <sys/types.h>
#include <unistd.h>

// int main(){
//     struct utmpx *ut;
//     struct passwd *pw;

//     setutxent();

//     while ((ut = getutxent()) != NULL){
//         if (ut->ut_type == USER_PROCESS){
//             pw = getpwnam(ut->ut_user);

//             if (pw != NULL){
//                 printf("%d (%s)\n", pw->pw_uid,ut->ut_user);
//             }
//             else{
//                 printf("? (%s)\n", ut->ut_user);
//             }
//         }
//     }
//     endutxent();

//     return 0;
// }

char **getUserGroups(const char* user, int* num_groups){
    struct  passwd *pw = getpwnam(user);
    if (!pw) return NULL;

    int ngroups = 0;
    gid_t gid = pw->pw_gid;
    getgrouplist(user,gid,NULL, &ngroups);
    gid_t *groups = malloc(ngroups * sizeof(gid_t));

    if (!groups) return NULL;
    if (getgrouplist(user,gid,groups,&ngroups) == -1){
        free(groups);
        return NULL;
    }

    char **group_names = malloc(ngroups *sizeof(char*));
    if (!group_names) return NULL;

    for(int i = 0; i < ngroups; i++){
        struct group *gr = getgrgid(groups[i]);
        if(gr){
            group_names[i] = strdup(gr->gr_name);
        }else{
            group_names[i] = strdup("unknown");
        }
    }

    *num_groups = ngroups;

    free(groups);
    return group_names;
}