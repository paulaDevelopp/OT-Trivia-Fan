from flask import Flask, request
import subprocess

app = Flask(__name__)

@app.route("/upload_wallpapers")
def upload_wallpapers():
    difficulty = request.args.get("difficulty")
    if difficulty not in ["easy", "medium", "difficult"]:
        return {"error": "Invalid difficulty"}, 400

    subprocess.run(["python3", "upload_wallpapers.py", difficulty])
    return {"status": "ok", "difficulty": difficulty}, 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
