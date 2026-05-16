#!/bin/bash

MOYS_SERVICE_NAME="MainSystemServiceKt"
JAVA_CMD="java -cp \".:*\""
JAVA_WSL_SAFEGUARD="--add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.java2d=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED"

if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || -n "$WINDIR" ]]; then
    echo "[INFO]: Windows detected"
    echo "[INFO]: WSL will be used to run MOys"
    if ! command -v wsl.exe &> /dev/null; then
        echo "[ERROR]: WSL is not installed or not in your PATH."
        exit 1
    fi
    echo "[INFO]: Detected WSL"
    WIN_PATH=$(pwd -W 2>/dev/null || pwd)
    WSL_PROJECT_DIR=$(MSYS_NO_PATHCONV=1 wsl.exe wslpath -u "$WIN_PATH")
    echo "[INFO]: Reevaluated path: \"$WSL_PROJECT_DIR\""
    echo "[INFO]: Executing MOys"
    MSYS_NO_PATHCONV=1 wsl.exe bash -c "cd '$WSL_PROJECT_DIR/out/artifacts/MOys_system_main_jar' && $JAVA_CMD $JAVA_WSL_SAFEGUARD $MOYS_SERVICE_NAME"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "[INFO]: Linux detected"
    echo "[INFO]: Executing MOys"
    eval "cd out/artifacts/MOys_system_main_jar"
    eval "$JAVA_CMD $MOYS_SERVICE_NAME"
else
    echo "[ERROR]: Unsupported OS: $OSTYPE"
    exit 1
fi
