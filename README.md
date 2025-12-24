🏋️ PoseTrainer – Admin Web

The PoseTrainer Admin Web is a web-based administration system for the PoseTrainer platform.
It allows administrators to manage users, exercises, training plans, and user feedback, ensuring smooth operation and data control for the entire system.

🔗 Main Repository:
https://github.com/thnhphng04/PoseTrainer-Fall2025

🚀 Technologies Used

Backend: Java Spring Boot

Frontend: Thymeleaf, HTML, CSS, JavaScript

Authentication: Firebase Authentication

Database: Firebase Storage / Firestore

API: RESTful API

Build Tool: Maven

✨ Key Features

🔐 User Management

Admin authentication using Firebase Authentication

View user list and user profiles

Activate / deactivate user accounts

Send mail when activate / deactivate user accounts

Role-based access control (Admin / User)

🏋️ Exercise & Training Plan Management

Manage exercises and workout templates

Manage training plans based on user goals and metrics


🤖 Manage community & User post

Show/hide post

Send mail when show or hide

💬 User Feedback Management

View and manage feedback submitted by users

Support system improvement based on user feedback

🏗️ System Architecture

Follows the MVC (Model–View–Controller) architecture

Clear separation of layers:

Controller: Handles admin requests

Service: Business logic processing

Repository: Firebase data access

Thymeleaf frontend communicates with Spring Boot backend via RESTful APIs
