#!/usr/bin/env python3
import requests
import json
import sys

API_URL = "https://api.openai.com/v1/chat/completions"
API_KEY = "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"
MODEL = "gpt-4o"

def call_chatgpt_api(user_message):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    
    data = {
        "model": MODEL,
        "messages": [{"role": "user", "content": user_message}],
        "temperature": 0.7
    }
    
    try:
        response = requests.post(API_URL, headers=headers, json=data)
        response.raise_for_status()  # Raise an exception for HTTP errors
        
        response_json = response.json()
        return response_json["choices"][0]["message"]["content"]
    except requests.exceptions.RequestException as e:
        return f"Error calling ChatGPT API: {str(e)}"
    except (KeyError, IndexError) as e:
        return f"Error parsing response: {str(e)}"

def main():
    print("ChatGPT Terminal Client")
    print("Type 'exit' to quit")
    print("---------------------------")
    
    while True:
        try:
            user_input = input("\nYou: ")
            
            if user_input.lower() == "exit":
                print("Goodbye!")
                break
            
            response = call_chatgpt_api(user_input)
            print(f"\nChatGPT: {response}")
        except KeyboardInterrupt:
            print("\nGoodbye!")
            break
        except Exception as e:
            print(f"An error occurred: {str(e)}")

if __name__ == "__main__":
    main()
