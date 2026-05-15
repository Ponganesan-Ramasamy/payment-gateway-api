# Pushing this project to GitHub

The project files are ready. Run these commands from your local machine after downloading the `payment-gateway-service` folder.

## 1. Create the repository on GitHub
- Go to https://github.com/new
- Repository name: `payment-gateway-service` (or your choice)
- Visibility: Private or Public
- **Do not** initialize with README/.gitignore — this project already has them
- Click **Create repository**

## 2. Initialize git locally and push

```bash
cd payment-gateway-service

git init
git add .
git commit -m "Initial commit: Spring Boot payment gateway microservice"
git branch -M main
git remote add origin https://github.com/<your-username>/payment-gateway-service.git
git push -u origin main
```

Replace `<your-username>` with your GitHub username (or org).

## 3. (Optional) Using GitHub CLI

If you have `gh` installed and authenticated:

```bash
cd payment-gateway-service
git init && git add . && git commit -m "Initial commit"
gh repo create payment-gateway-service --private --source=. --push
```

## 4. Authentication tips
- HTTPS push will prompt for credentials — use a **Personal Access Token** (Settings → Developer settings → Tokens) in place of your password.
- Or set up SSH and use `git@github.com:<your-username>/payment-gateway-service.git` as the remote.
