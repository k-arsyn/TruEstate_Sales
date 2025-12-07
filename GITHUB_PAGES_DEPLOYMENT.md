# Deploy Frontend to GitHub Pages - Complete Guide

## Your Backend URL
https://truestate-sales.onrender.com

## What I've Configured

I've already updated your files for GitHub Pages deployment:
- ✅ `vite.config.mts` - Added base path and build configuration
- ✅ `package.json` - Added deploy script and gh-pages dependency
- ✅ `.env.production` - Already has your Render backend URL

## Deployment Steps

### Step 1: Install gh-pages Package

```bash
cd D:\TruEstate\frontend
npm install
```

This will install the `gh-pages` package I added to your `package.json`.

### Step 2: Push Changes to GitHub

```bash
cd D:\TruEstate
git add .
git commit -m "Configure for GitHub Pages deployment"
git push origin main
```

### Step 3: Deploy to GitHub Pages

```bash
cd D:\TruEstate\frontend
npm run deploy
```

This command will:
1. Build your React app
2. Create a `gh-pages` branch automatically
3. Push the built files to that branch
4. Deploy to GitHub Pages

### Step 4: Enable GitHub Pages

1. Go to your GitHub repository: **https://github.com/k-arsyn/TruEstate_Sales**
2. Click **"Settings"** tab
3. Scroll down to **"Pages"** in the left sidebar
4. Under **"Source"**, select:
   - Branch: `gh-pages`
   - Folder: `/ (root)`
5. Click **"Save"**
6. Wait 1-2 minutes for deployment

### Step 5: Get Your Live URL

Your frontend will be live at:
```
https://k-arsyn.github.io/TruEstate_Sales/
```

GitHub will show this URL in the Pages settings once deployed.

### Step 6: Update Backend CORS

1. Go to **Render Dashboard**: https://dashboard.render.com
2. Click on **truestate-sales** service
3. Go to **"Environment"** tab
4. Find `ALLOWED_ORIGINS` variable
5. Click **"Edit"**
6. Update value to:
   ```
   https://k-arsyn.github.io,http://localhost:5173
   ```
7. Click **"Save Changes"**
8. Wait ~2 minutes for redeployment

## Testing Your Deployment

1. Visit: **https://k-arsyn.github.io/TruEstate_Sales/**
2. Open DevTools (F12) → Network tab
3. Test all features:
   - ✅ Search
   - ✅ Filters
   - ✅ Sorting
   - ✅ Pagination
   - ✅ Copy phone

## Future Updates

Whenever you make changes to the frontend:

```bash
cd D:\TruEstate\frontend
npm run deploy
```

This automatically rebuilds and redeploys to GitHub Pages!

## Comparison: GitHub Pages vs Vercel

| Feature | GitHub Pages | Vercel |
|---------|-------------|--------|
| **Cost** | Completely Free | Free (100GB bandwidth) |
| **Setup** | Manual deploy command | Auto-deploy on push |
| **Custom Domain** | Yes (free) | Yes (free) |
| **Build Time** | ~1 minute | ~2-3 minutes |
| **URL Format** | username.github.io/repo | random.vercel.app |
| **HTTPS** | Yes (automatic) | Yes (automatic) |

## Advantages of GitHub Pages

- ✅ Completely free forever
- ✅ No bandwidth limits
- ✅ Simple deployment with one command
- ✅ Integrated with your repository
- ✅ No third-party account needed
- ✅ Fast CDN delivery

## Your Live Application

Once deployed:
- **Frontend**: https://k-arsyn.github.io/TruEstate_Sales/
- **Backend**: https://truestate-sales.onrender.com

## Troubleshooting

### Blank Page After Deployment
- Check browser console for errors
- Verify `base` path in `vite.config.mts` matches your repo name
- Ensure `VITE_API_URL` is in `.env.production`

### CORS Errors
- Update Render backend `ALLOWED_ORIGINS` to include `https://k-arsyn.github.io`
- No trailing slash in the origin URL

### 404 on Refresh
- GitHub Pages doesn't support client-side routing perfectly
- Main page will work fine
- For better SPA support, consider adding a 404.html redirect

### Changes Not Showing
- Clear browser cache (Ctrl + Shift + R)
- Wait 1-2 minutes after `npm run deploy`
- Check GitHub Actions for deployment status

---

**Ready to deploy? Just run these commands:**

```bash
# Step 1: Install dependencies
cd D:\TruEstate\frontend
npm install

# Step 2: Push to GitHub
cd D:\TruEstate
git add .
git commit -m "Configure for GitHub Pages"
git push origin main

# Step 3: Deploy
cd D:\TruEstate\frontend
npm run deploy
```

That's it! Your app will be live in 2-3 minutes! 🚀

