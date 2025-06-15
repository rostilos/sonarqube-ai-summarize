#!/bin/bash
SONARQUBE_VERSION="24.12.0.100206"
SONARQUBE_APP_JAR="sonar-application-${SONARQUBE_VERSION}.jar"
DESTINATION_DIR="./lib"
SONARQUBE_PLUGIN_DIR="./"

DOWNLOAD_URL="https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-${SONARQUBE_VERSION}.zip"
TEMP_DOWNLOAD_DIR="./temp_sonarqube_download"
ZIP_FILE_NAME="sonarqube-${SONARQUBE_VERSION}.zip"
FULL_DOWNLOAD_PATH="${TEMP_DOWNLOAD_DIR}/${ZIP_FILE_NAME}"
TARGET_JAR_PATH="${DESTINATION_DIR}/${SONARQUBE_APP_JAR}"

echo "--- SonarQube Application JAR Downloader & Plugin Builder ---"
echo "Target SonarQube Version: ${SONARQUBE_VERSION}"
echo "Desired Application JAR: ${SONARQUBE_APP_JAR}"
echo "JAR Destination: ${DESTINATION_DIR}"
echo "SonarQube Plugin Project: ${SONARQUBE_PLUGIN_DIR}"

JAR_ACQUIRED=0

if [ -f "${TARGET_JAR_PATH}" ]; then
    echo "SonarQube application JAR '${SONARQUBE_APP_JAR}' already exists in '${DESTINATION_DIR}'. Skipping download and extraction."
    JAR_ACQUIRED=1
else
    echo "Creating temporary download directory: ${TEMP_DOWNLOAD_DIR}"
    mkdir -p "${TEMP_DOWNLOAD_DIR}" || { echo "ERROR: Failed to create directory '${TEMP_DOWNLOAD_DIR}'. Check permissions."; exit 1; }

    echo "Creating destination directory: ${DESTINATION_DIR}"
    mkdir -p "${DESTINATION_DIR}" || { echo "ERROR: Failed to create directory '${DESTINATION_DIR}'. Check permissions."; rm -rf "${TEMP_DOWNLOAD_DIR}"; exit 1; }

    echo "Downloading SonarQube ${SONARQUBE_VERSION} distribution from '${DOWNLOAD_URL}'..."
    curl --fail -L -o "${FULL_DOWNLOAD_PATH}" "${DOWNLOAD_URL}" || { echo "ERROR: Failed to download SonarQube distribution. Check URL and network connection."; rm -rf "${TEMP_DOWNLOAD_DIR}"; exit 1; }

    echo "Extracting only '${SONARQUBE_APP_JAR}'..."
    TEMP_EXTRACT_DIR="${TEMP_DOWNLOAD_DIR}/extracted_full_distro"
    mkdir -p "${TEMP_EXTRACT_DIR}"
    unzip -q "${FULL_DOWNLOAD_PATH}" -d "${TEMP_EXTRACT_DIR}" || { echo "ERROR: Failed to extract '${FULL_DOWNLOAD_PATH}'. Is 'unzip' installed?"; rm -rf "${TEMP_DOWNLOAD_DIR}"; exit 1; }

    FOUND_JAR_PATH=$(find "${TEMP_EXTRACT_DIR}" -type f -name "${SONARQUBE_APP_JAR}" | head -n 1)

    if [ -z "${FOUND_JAR_PATH}" ]; then
        echo "ERROR: Could not find '${SONARQUBE_APP_JAR}' inside the extracted SonarQube distribution."
        echo "Please verify the SONARQUBE_APP_JAR name and SONARQUBE_VERSION."
        rm -rf "${TEMP_DOWNLOAD_DIR}"
        exit 1
    fi

    echo "Found JAR at: ${FOUND_JAR_PATH}"
    echo "Moving JAR to final destination: ${TARGET_JAR_PATH}"
    mv "${FOUND_JAR_PATH}" "${TARGET_JAR_PATH}" || { echo "ERROR: Failed to move '${FOUND_JAR_PATH}' to '${DESTINATION_DIR}'."; rm -rf "${TEMP_DOWNLOAD_DIR}"; exit 1; }

    echo "Cleaning up temporary files and directories..."
    rm -rf "${TEMP_DOWNLOAD_DIR}" || { echo "WARNING: Could not remove temporary directory '${TEMP_DOWNLOAD_DIR}'."; }

    echo "--- Successfully placed '${SONARQUBE_APP_JAR}' in '${DESTINATION_DIR}' ---"
    JAR_ACQUIRED=1
fi

if [ ${JAR_ACQUIRED} -eq 1 ]; then
    if [ ${SKIP_BUILD} -eq 1 ]; then
      echo "--- Skip Building SonarQube Plugin ---"
      exit 0
    fi
    echo ""
    echo "--- Building SonarQube Plugin ---"
    echo "Navigating to plugin directory: ${SONARQUBE_PLUGIN_DIR}"
    if [ ! -d "${SONARQUBE_PLUGIN_DIR}" ]; then
        echo "ERROR: SonarQube plugin directory '${SONARQUBE_PLUGIN_DIR}' not found. Please configure 'SONARQUBE_PLUGIN_DIR' correctly."
        exit 1
    fi


    cd "${SONARQUBE_PLUGIN_DIR}" || { echo "ERROR: Failed to change directory to '${SONARQUBE_PLUGIN_DIR}'."; exit 1; }
    echo "Running 'mvn clean package' for the plugin..."
    mvn clean package || { echo "ERROR: Maven build failed in '${SONARQUBE_PLUGIN_DIR}'."; exit 1; }
    echo "--- SonarQube Plugin Build Complete ---"
else
    echo "Maven build skipped because the SonarQube application JAR could not be acquired."
    exit 1
fi