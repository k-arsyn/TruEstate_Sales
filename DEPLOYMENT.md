# Deployment Guide - Vercel (Frontend) + Render (Backend)

## Prerequisites

- GitHub account
- Git installed locally
- Code pushed to GitHub repository

---

## Part 1: Deploy Backend to Render

### Step 1: Prepare Backend (Already Done)

The following files have been created/updated:
- `backend/application.properties` - Uses environment variables
- `backend/config/CorsConfig.java` - Reads allowed origins from env
- `backend/Procfile` - Tells Render how to start the app
- `backend/build.sh` - Build script for Render

### Step 2: Push Code to GitHub

```bash
cd D:\TruEstate
git add .
git commit -m "Prepare for deployment on Render and Vercel"
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
- Runtime: `Java`
- Build Command: `./mvnw clean package -DskipTests`
- Start Command: `java -jar target/retail-backend-0.0.1-SNAPSHOT.jar`

**Instance Type:**
- Select "Free"

**Environment Variables** (Click "Advanced" → "Add Environment Variable"):
```
PORT = 8080
H2_CONSOLE_ENABLED = false
ALLOWED_ORIGINS = https://your-app-name.vercel.app
```

Note: You'll update `ALLOWED_ORIGINS` after deploying frontend

6. Click "Create Web Service"
7. Wait 5-10 minutes for build and deployment
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
ALLOWED_ORIGINS = https://truestate.vercel.app,http://localhost:5173
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
PORT = 8080
H2_CONSOLE_ENABLED = false
ALLOWED_ORIGINS = https://truestate.vercel.app,http://localhost:5173
```

### Frontend (Vercel)
```
VITE_API_URL = https://truestate-backend.onrender.com/api/sales
```

---

## Important Notes

### Render Free Tier Limitations
- Service sleeps after 15 minutes of inactivity
- First request after sleep takes 30-60 seconds (cold start)
- 750 hours/month free (enough for one service)

### Vercel Free Tier Limitations
- 100GB bandwidth/month
- Unlimited deployments
- Custom domain supported

### Cold Start Solution
If cold starts are annoying, you can:
1. Use a free uptime monitoring service (e.g., UptimeRobot) to ping your backend every 10 minutes
2. Upgrade to Render paid tier ($7/month for always-on service)

---

## Troubleshooting

### Backend Issues

**Build fails on Render:**
- Check Java version in Render settings (should be Java 17)
- Verify Maven wrapper exists: `backend/mvnw`
- Check build logs in Render dashboard

**CORS errors:**
- Verify `ALLOWED_ORIGINS` includes your Vercel URL
- Check for trailing slashes (should not have them)
- Verify frontend URL matches exactly

**CSV data not loading:**
- Ensure `sales_data.csv` is in `backend/src/main/resources/`
- Check file is included in build: verify in `target/classes/`

### Frontend Issues

**API calls failing:**
- Verify `VITE_API_URL` environment variable in Vercel
- Check Network tab for exact error
- Confirm backend URL is accessible

**Environment variable not working:**
- Environment variables must start with `VITE_`
- Redeploy after adding environment variables
- Clear browser cache

**Build fails on Vercel:**
- Check Node.js version (should be 16+)
- Verify `package.json` scripts are correct
- Check build logs in Vercel dashboard

---

## Local Development After Deployment

Backend still runs on `http://localhost:8080`:
```bash
cd backend
mvn spring-boot:run
```

Frontend still runs on `http://localhost:5173`:
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
Render automatically rebuilds and redeploys.

### Update Frontend
```bash
# Make changes to frontend code
git add frontend/
git commit -m "Update frontend"
git push origin main
```
Vercel automatically rebuilds and redeploys.

---

## Post-Deployment Checklist

- [ ] Backend deployed on Render
- [ ] Backend URL accessible: `https://your-app.onrender.com/api/sales`
- [ ] Frontend deployed on Vercel
- [ ] Frontend URL accessible: `https://your-app.vercel.app`
- [ ] CORS configured correctly
- [ ] Environment variables set on both platforms
- [ ] All features working (search, filter, sort, pagination)
- [ ] No console errors in browser
- [ ] API calls successful in Network tab
- [ ] Update project README with live URLs

---

## Live URLs Template

Add this to your main README.md:

```markdown
## Live Demo

- Frontend: https://truestate.vercel.app
- Backend API: https://truestate-backend.onrender.com/api/sales

Note: Backend may take 30-60 seconds to wake up on first request (Render free tier limitation)
```

---

Your application is now live and accessible worldwide!

