# Frontend Deployment to Vercel - Quick Guide

## Your Backend URL
https://truestate-sales.onrender.com

## Step 1: Push Updated Configuration to GitHub

```bash
cd D:\TruEstate
git add frontend/.env.production
git commit -m "Configure production API URL for Vercel"
git push origin main
```

## Step 2: Deploy to Vercel

### A. Create Account
1. Go to **https://vercel.com**
2. Click **"Sign Up"**
3. Choose **"Continue with GitHub"**
4. Authorize Vercel to access your repositories

### B. Import Project
1. Click **"Add New..."** → **"Project"**
2. Find your repository in the list
3. Click **"Import"** next to your repository name

### C. Configure Build Settings
1. **Framework Preset**: Vite (auto-detected)
2. **Root Directory**: 
   - Click **"Edit"** button
   - Type: `frontend`
   - Click **"Continue"**
3. **Build Settings** (should auto-fill):
   - Build Command: `npm run build`
   - Output Directory: `dist`
   - Install Command: `npm install`

### D. Add Environment Variable
1. Expand **"Environment Variables"** section
2. Add variable:
   - **Name**: `VITE_API_URL`
   - **Value**: `https://truestate-sales.onrender.com/api/sales`
3. Select all environments: ✅ Production ✅ Preview ✅ Development
4. Click **"Add"**

### E. Deploy
1. Click **"Deploy"** button
2. Wait 2-3 minutes for build to complete
3. You'll see "Congratulations!" with your live URL

### F. Copy Your URL
Your frontend will be at: `https://YOUR-PROJECT-NAME.vercel.app`

## Step 3: Update Backend CORS

Once you have your Vercel URL:

1. Go to **Render Dashboard** (https://dashboard.render.com)
2. Click on **truestate-sales** service
3. Go to **"Environment"** tab
4. Find `ALLOWED_ORIGINS` variable
5. Click **"Edit"**
6. Update value to:
   ```
   https://YOUR-VERCEL-URL.vercel.app,http://localhost:5173
   ```
   (Replace YOUR-VERCEL-URL with actual URL from Vercel)
7. Click **"Save Changes"**
8. Wait ~2 minutes for automatic redeployment

## Step 4: Test Your Live Application

1. Visit your Vercel URL
2. Press F12 to open DevTools
3. Go to **Network** tab
4. Test these features:
   - ✅ Search by name/phone
   - ✅ Apply filters (region, gender, etc.)
   - ✅ Change sorting
   - ✅ Navigate pages
   - ✅ Copy phone number
5. Verify API calls show status **200 OK**

## Troubleshooting

### CORS Error
- Make sure ALLOWED_ORIGINS on Render includes your exact Vercel URL
- No trailing slash in URLs
- Wait 2 minutes after updating for redeployment

### API Calls Fail
- Check Network tab for error details
- Verify VITE_API_URL is set correctly in Vercel
- Test backend directly: https://truestate-sales.onrender.com/api/sales?page=0&size=5

### Backend Cold Start
- First request may take 30-60 seconds if backend was sleeping
- Subsequent requests will be fast

## Complete! 🎉

Your application is now live:
- **Backend**: https://truestate-sales.onrender.com
- **Frontend**: https://your-app.vercel.app

Share your Vercel URL once deployment completes!

