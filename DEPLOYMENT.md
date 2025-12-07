# Deployment Guide - Vercel (Frontend) + Render (Backend)

## Prerequisites

- GitHub account
- Git installed locally
- Code pushed to GitHub repository

---

## Part 1: Deploy Backend to Render (Using Docker)

### Step 1: Prepare Backend (Already Done)

The following files have been created/updated:
- `backend/application.properties` - Uses environment variables
- `backend/config/CorsConfig.java` - Reads allowed origins from env
- `backend/Dockerfile` - Docker configuration for Render
- `backend/.dockerignore` - Excludes unnecessary files from Docker build

### Step 2: Push Code to GitHub

```bash
cd D:\TruEstate
git add .
git commit -m "Add Docker configuration for Render deployment"
git push origin main
```

### Step 3: Create Render Account and Deploy

1. Go to https://render.com
2. Click "Get Started" and sign up with GitHub
3. Click "New +" → "Web Service"
4. Click "Connect" next to your GitHub repository
5. Configure the service:

**Settings:**
- Name: `truestate-backend` (or any name)
- Region: `Oregon (US West)` or closest to you
- Branch: `main`
- Root Directory: `backend`
- Environment: `Docker`
- Dockerfile Path: `./Dockerfile`

**Instance Type:**
- Select "Free"

**Environment Variables** (Click "Advanced" → "Add Environment Variable"):
```
PORT=8080
H2_CONSOLE_ENABLED=false
ALLOWED_ORIGINS=https://your-app-name.vercel.app,http://localhost:5173
```

Note: You'll update `ALLOWED_ORIGINS` after deploying frontend

6. Click "Create Web Service"
7. Wait 5-10 minutes for Docker build and deployment
8. Copy the URL (will be like `https://truestate-backend.onrender.com`)

### Step 4: Test Backend

Visit: `https://truestate-backend.onrender.com/api/sales?page=0&size=10`

You should see JSON response with sales data.

---

## Part 2: Deploy Frontend to Vercel

### Step 1: Prepare Frontend (Already Done)

The following files have been created/updated:
- `frontend/src/services/salesApi.js` - Uses `VITE_API_URL` env variable
- `frontend/.env.local` - Local development config
- `frontend/.env.production` - Production config template
- `frontend/.gitignore` - Excludes sensitive files

### Step 2: Update Production Environment File

Edit `frontend/.env.production` and replace with your Render backend URL:

```env
VITE_API_URL=https://truestate-backend.onrender.com/api/sales
```

Replace `truestate-backend.onrender.com` with your actual Render URL.

### Step 3: Commit Changes

```bash
git add frontend/.env.production
git commit -m "Update production API URL"
git push origin main
```

### Step 4: Create Vercel Account and Deploy

1. Go to https://vercel.com
2. Click "Sign Up" and choose "Continue with GitHub"
3. Click "Add New..." → "Project"
4. Import your GitHub repository
5. Configure the project:

**Project Settings:**
- Framework Preset: `Vite`
- Root Directory: `frontend`
- Build Command: `npm run build` (auto-detected)
- Output Directory: `dist` (auto-detected)
- Install Command: `npm install` (auto-detected)

**Environment Variables:**
Click "Environment Variables" and add:
```
Name: VITE_API_URL
Value: https://truestate-backend.onrender.com/api/sales
```
(Use your actual Render backend URL)

Select: Production, Preview, Development (all environments)

6. Click "Deploy"
7. Wait 2-3 minutes for deployment
8. Copy the Vercel URL (will be like `https://truestate.vercel.app`)

### Step 5: Update CORS Configuration

1. Go back to Render dashboard
2. Click on your backend service
3. Go to "Environment" tab
4. Update `ALLOWED_ORIGINS` variable:
```
ALLOWED_ORIGINS=https://truestate.vercel.app,http://localhost:5173
```
(Replace with your actual Vercel URL, keep localhost for local dev)

5. Click "Save Changes"
6. Service will automatically redeploy

---

## Part 3: Verify Deployment

### Test Backend
```bash
curl https://truestate-backend.onrender.com/api/sales?page=0&size=5
```

Expected: JSON response with sales data

### Test Frontend

1. Visit your Vercel URL: `https://truestate.vercel.app`
2. Open browser DevTools (F12) → Network tab
3. You should see:
   - Page loads successfully
   - API calls to Render backend
   - Data displayed in table
4. Test all features:
   - Search functionality
   - Filter dropdowns
   - Sorting
   - Pagination
   - Copy phone number

---

## Environment Variables Summary

### Backend (Render)
```
PORT=8080
H2_CONSOLE_ENABLED=false
ALLOWED_ORIGINS=https://truestate.vercel.app,http://localhost:5173
```

### Frontend (Vercel)
```
VITE_API_URL=https://truestate-backend.onrender.com/api/sales
```

---

## Important Notes

### Render Free Tier Limitations
- Service sleeps after 15 minutes of inactivity
- First request after sleep takes 30-60 seconds (cold start)
- 750 hours/month free (enough for one service)
- Docker build takes 5-10 minutes on first deploy

### Vercel Free Tier Limitations
- 100GB bandwidth/month
- Unlimited deployments
- Custom domain supported

### Cold Start Solution
If cold starts are annoying, you can:
1. Use a free uptime monitoring service (e.g., UptimeRobot) to ping your backend every 10 minutes
2. Upgrade to Render paid tier ($7/month for always-on service)

---

## Docker Build Information

The backend uses a multi-stage Docker build:
- **Build Stage**: Uses `eclipse-temurin:17-jdk-alpine` to compile the application
- **Runtime Stage**: Uses `eclipse-temurin:17-jre-alpine` (smaller image) to run the application
- **Optimizations**: 
  - Layers are cached for faster rebuilds
  - JRE-only runtime reduces image size by ~150MB
  - Alpine Linux base for minimal footprint

---

## Troubleshooting

### Backend Issues

**Docker build fails on Render:**
- Check build logs in Render dashboard under "Events" tab
- Verify `Dockerfile` is in `backend/` directory
- Ensure Maven wrapper (`mvnw`) has execute permissions
- Check if `pom.xml` specifies Java 17

**Container starts but crashes:**
- Check "Logs" tab in Render dashboard
- Verify `PORT` environment variable is set to 8080
- Ensure `application.properties` uses `${PORT:8080}`
- Check if CSV file is included in Docker image

**CORS errors:**
- Verify `ALLOWED_ORIGINS` includes your Vercel URL (no trailing slash)
- Check for typos in URLs
- Verify frontend URL matches exactly
- Wait 30 seconds after updating environment variables for redeployment

**CSV data not loading:**
- Ensure `sales_data.csv` is in `backend/src/main/resources/`
- Check Docker build logs to confirm file is copied
- Verify file path in `CsvDataLoader.java`

### Frontend Issues

**API calls failing:**
- Verify `VITE_API_URL` environment variable in Vercel
- Check Network tab for exact error (404, 500, CORS, etc.)
- Confirm backend URL is accessible by visiting it directly
- Check if backend is awake (first request may take 60 seconds)

**Environment variable not working:**
- Environment variables must start with `VITE_`
- Redeploy after adding environment variables (not automatic)
- Clear browser cache and hard reload (Ctrl+Shift+R)
- Check Vercel build logs to confirm variable was set

**Build fails on Vercel:**
- Check Node.js version (should be 16+)
- Verify `package.json` scripts are correct
- Check build logs in Vercel dashboard for specific errors
- Ensure all dependencies are in `package.json`

**CORS errors in browser:**
- Backend must include your exact Vercel URL in `ALLOWED_ORIGINS`
- Check browser console for the blocked origin
- Verify backend CORS configuration was updated and redeployed

---

## Local Development After Deployment

### Backend (runs on http://localhost:8080)

**With Maven:**
```bash
cd backend
mvn spring-boot:run
```

**With Docker (test Docker build locally):**
```bash
cd backend
docker build -t truestate-backend .
docker run -p 8080:8080 -e H2_CONSOLE_ENABLED=true truestate-backend
```

### Frontend (runs on http://localhost:5173)

```bash
cd frontend
npm run dev
```

Environment variables automatically use `.env.local` for development.

---

## Updating Your Deployment

### Update Backend
```bash
# Make changes to backend code
git add backend/
git commit -m "Update backend"
git push origin main
```
Render automatically rebuilds Docker image and redeploys (takes 5-10 minutes).

### Update Frontend
```bash
# Make changes to frontend code
git add frontend/
git commit -m "Update frontend"
git push origin main
```
Vercel automatically rebuilds and redeploys (takes 2-3 minutes).

### Update Environment Variables

**Render:**
1. Go to service → "Environment" tab
2. Edit or add variables
3. Click "Save" (auto-redeploys)

**Vercel:**
1. Go to project → "Settings" → "Environment Variables"
2. Edit or add variables
3. Go to "Deployments" tab → Click "..." → "Redeploy"

---

## Post-Deployment Checklist

- [ ] Backend Dockerfile created
- [ ] Backend deployed on Render
- [ ] Backend URL accessible: `https://your-app.onrender.com/api/sales`
- [ ] Docker container running (check Render logs)
- [ ] Frontend deployed on Vercel
- [ ] Frontend URL accessible: `https://your-app.vercel.app`
- [ ] CORS configured correctly (both URLs in ALLOWED_ORIGINS)
- [ ] Environment variables set on both platforms
- [ ] All features working (search, filter, sort, pagination)
- [ ] No console errors in browser
- [ ] API calls successful in Network tab
- [ ] CSV data loading correctly
- [ ] Phone copy functionality working
- [ ] Update project README with live URLs

---

## Live URLs Template

Add this to your main README.md:

```markdown
## Live Demo

- **Frontend**: https://truestate.vercel.app
- **Backend API**: https://truestate-backend.onrender.com/api/sales

**Note**: Backend may take 30-60 seconds to wake up on first request due to Render free tier limitations (cold start). Subsequent requests are fast.

### Test the API
```bash
curl "https://truestate-backend.onrender.com/api/sales?page=0&size=5"
```
```

---

## Alternative: Deploy Backend to Railway (No Docker Required)

If you encounter issues with Docker on Render, Railway is an excellent alternative that auto-detects Java/Maven:

1. Go to https://railway.app
2. Sign up with GitHub
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Add environment variables:
   ```
   PORT=8080
   H2_CONSOLE_ENABLED=false
   ALLOWED_ORIGINS=https://your-vercel-url.vercel.app
   ```
6. Railway auto-builds and deploys (no Docker config needed)
7. Generate domain and use that URL for frontend

**Railway Advantages:**
- No Docker required (auto-detects Maven)
- Faster builds
- $5 free credit monthly
- No cold starts within credit limit

---

Your application is now ready for deployment with Docker on Render and Vercel!
