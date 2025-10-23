#!/bin/bash

# Digital Wallet - Gmail Setup Script
# This script helps configure Gmail credentials for OTP service

echo "=========================================="
echo "Digital Wallet - Gmail Setup"
echo "=========================================="
echo ""

# Check if running in interactive mode
if [ -t 0 ]; then
    echo "Please provide your Gmail credentials:"
    echo ""
    
    read -p "Enter your Gmail address: " gmail_username
    read -s -p "Enter your Gmail App Password (16 characters): " gmail_password
    echo ""
    
    if [ -z "$gmail_username" ] || [ -z "$gmail_password" ]; then
        echo "Error: Both Gmail username and app password are required."
        exit 1
    fi
    
    if [ ${#gmail_password} -lt 8 ]; then
        echo "Error: Password must be at least 8 characters long."
        exit 1
    fi
    
    # Set environment variables
    export GMAIL_USERNAME="$gmail_username"
    export GMAIL_APP_PASSWORD="$gmail_password"
    
    echo ""
    echo "Environment variables set for current session:"
    echo "GMAIL_USERNAME=$GMAIL_USERNAME"
    echo "GMAIL_APP_PASSWORD=${GMAIL_APP_PASSWORD:0:4}****${GMAIL_APP_PASSWORD: -4}"
    echo ""
    
    # Add to shell profile
    read -p "Add to shell profile for permanent setup? (y/n): " add_to_profile
    if [[ $add_to_profile =~ ^[Yy]$ ]]; then
        profile_file=""
        if [[ "$SHELL" == *"zsh"* ]]; then
            profile_file="$HOME/.zshrc"
        elif [[ "$SHELL" == *"bash"* ]]; then
            profile_file="$HOME/.bashrc"
        else
            profile_file="$HOME/.profile"
        fi
        
        if [ -n "$profile_file" ]; then
            # shellcheck disable=SC2129
            echo "" >> "$profile_file"
            echo "# Digital Wallet Gmail Configuration" >> "$profile_file"
            echo "export GMAIL_USERNAME=\"$gmail_username\"" >> "$profile_file"
            echo "export GMAIL_APP_PASSWORD=\"$gmail_password\"" >> "$profile_file"
            echo ""
            echo "Added to $profile_file"
            echo "Run 'source $profile_file' to apply changes in current session"
        fi
    fi
    
    echo ""
    echo "Setup complete! You can now start the Digital Wallet application."
    echo "The email service will automatically use these credentials."
    echo ""
    echo "To test the setup, use the test endpoints:"
    echo "  POST /api/v1/test/email/test"
    echo "  POST /api/v1/test/email/test-otp?email=$gmail_username&purpose=KYC"
    
else
    echo "Non-interactive mode detected."
    echo "Please set the following environment variables manually:"
    echo "  export GMAIL_USERNAME=\"your-email@gmail.com\""
    echo "  export GMAIL_APP_PASSWORD=\"your-16-character-app-password\""
    echo ""
    echo "Or run this script in interactive mode."
fi

