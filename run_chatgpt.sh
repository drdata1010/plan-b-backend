#!/bin/bash
# Make the Python script executable
chmod +x chatgpt_client.py

# Activate the virtual environment and run the Python script
source chatgpt_venv/bin/activate
python chatgpt_client.py
deactivate
