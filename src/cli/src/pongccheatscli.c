#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <sys/ptrace.h>
#include <sys/wait.h>
#include <sys/user.h>
#include <sys/reg.h>

#define DRAW_SCOREBOARD_OFFSET 0x1f80

int parse_score(int argc, char* score_str) {
    if (argc < 3) {
        return -1;
    }

    for (size_t i = 0; i < strlen(score_str); i++) {
        if (!isdigit(score_str[i])) {
            return -1;
        }
    }

    int score;
    sscanf(score_str, "%d", &score);

    if (score < 0 || score > 999999) {
        return -1;
    }

    return score;
}
int main(int argc, char* argv[]) {
    int left_score_set = parse_score(argc, argv[1]);
    int right_score_set = parse_score(argc, argv[2]);

    if (left_score_set == -1 || right_score_set == -1) {
        printf("pongccheatscli - alternate pong c cheats implementation\n\n");
        printf("USAGE:\n");
        printf("  pongccheatscli <left_score> <right_score>\n");
        printf("    <left_score> - score for the left player in range 0 to 999999\n");
        printf("    <right_score> - score for the right player in range 0 to 999999\n\n");
        printf("NOTE:\n");
        printf("  pongccheatscli does not depend on pongccheatsd\n");

        return 1;
    }

    FILE* process = popen("/usr/bin/env pidof Pong\\ C", "r");

    if (process == NULL) {
        fprintf(stderr, "an error occurring when finding the pong c pid!\n");
        return 1;
    }

    int i = 0;
    char ch;

    char pid_arr[8];
    while ((ch = fgetc(process)) != EOF) {
        pid_arr[i++] = ch;
    }

    fclose(process);

    pid_t pid;
    sscanf(pid_arr, "%d", &pid);

    if (pid == 0) {
        fprintf(stderr, "could not find pong c pid. is it running?\n");
        return 1;
    }


    char proc_maps_path[20];
    sprintf(proc_maps_path, "/proc/%d/maps", pid);

    FILE* maps = fopen(proc_maps_path, "r");
    if (maps == NULL) {
        fprintf(stderr, "could not open the maps!\n");
        return 1;
    }

    i = 0;

    char base_hex[13];
    while ((ch = fgetc(maps)) != '-') {
        base_hex[i++] = ch;
    }

    fclose(maps);

    long base = strtol(base_hex, NULL, 16);

    if (ptrace(PTRACE_ATTACH, pid, NULL, NULL) == -1) {
        fprintf(stderr, "ptrace attach failed!");
        return 1;
    }

    waitpid(pid, NULL, WUNTRACED);

    long data = ptrace(PTRACE_PEEKDATA, pid, base + DRAW_SCOREBOARD_OFFSET, NULL);

    if (data == -1) {
        fprintf(stderr, "ptrace peek data failed!\n");
        return 1;
    }

    long data_w_int3 = ((data & ~0xff) | 0xcc);

    if (ptrace(PTRACE_POKEDATA, pid, base + DRAW_SCOREBOARD_OFFSET, data_w_int3) == -1) {
        fprintf(stderr, "ptrace poke data failed\n");
        return 1;
    }

    if (ptrace(PTRACE_CONT, pid, NULL, NULL) == -1) {
        fprintf(stderr, "ptrace cont failed");
        return 1;
    }

    while (true) {
        int status;
        waitpid(pid, &status, WUNTRACED);

        if (WIFSTOPPED(status)) {
            int signal = WSTOPSIG(status);

            if (signal == SIGTRAP) {
                struct user_regs_struct registers;
                if (ptrace(PTRACE_GETREGS, pid, NULL, &registers) == -1) {
                    fprintf(stderr, "ptrace get regs failed!\n");
                    return 1;
                }

                registers.rdi = left_score_set;
                registers.rsi = right_score_set;

                if (ptrace(PTRACE_SETREGS, pid, NULL, &registers) == -1) {
                    fprintf(stderr, "ptrace set regs failed!\n");
                    return 1;
                }
                if (ptrace(PTRACE_CONT, pid, NULL, NULL) == -1) {
                    fprintf(stderr, "ptrace cont failed!\n");
                    return 1;
                }
            } else {
                if (ptrace(PTRACE_CONT, pid, NULL, signal) == -1) {
                    fprintf(stderr, "ptrace cont failed!\n");
                    return 1;
                }
            }
        }
    }

}
