# Build the jar without installing anything

1. Create a free account at github.com and make a **new empty repository** (private is fine).
2. Click "uploading an existing file" on the new repo page and drag in the
   **contents** of this folder (including the hidden `.github` folder), then commit.
   Tip: if drag-and-drop skips `.github`, use GitHub Desktop or `git push` instead.
3. Open the repo's **Actions** tab. The "Build Hearthvale" workflow runs automatically
   (or click "Run workflow"). It takes a few minutes the first time.
4. When it finishes, open the run and download the **hearthvale-jar** artifact at the bottom.
   Unzip it to get `hearthvale-0.1.0.jar`.
5. Install NeoForge for Minecraft 1.21.1, then put the jar in `.minecraft/mods`.
