# ~/.bashrc: executed by bash(1) for non-login shells.

fgc() {
    printf "\[\e[38;5;${1}m\]"
}

bgc() {
    resetc
    printf "\[\e[48;5;${1}m\]"
}

resetc() {
    printf "\[\e[0m\]"
}

export PS1="$(bgc 253)$(fgc 53)citadel$(bgc 253):\w \\$`resetc` "
umask 022
shopt -s checkwinsize

export LS_OPTIONS='--color=auto'
eval `dircolors`
alias ls='ls $LS_OPTIONS'
alias ll='ls $LS_OPTIONS -l'

source /usr/share/bash-completion/bash_completion
