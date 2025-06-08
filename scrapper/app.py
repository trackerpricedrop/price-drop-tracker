from flask import Flask, request, jsonify
from scrapping import scrape_batch

app = Flask(__name__)

@app.route("/scrape", methods=["POST"])
def scrape():
    data = request.get_json()
    urls = data.get("urls")

    if not urls or not isinstance(urls, list):
        return jsonify({"error": "Invalid or missing 'urls'"}), 400

    results = scrape_batch(urls)
    return jsonify(results)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8110)
