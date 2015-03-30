(ns image-downloader.core
  (:require [clj-http.client :as client]))

(use 'clojure.java.io)

(defn- get-content [url]
  (let [res (client/get url)]
    (if (= (:status res) 200)
      (:body res))))

(defn- get-content-as-byte-array [url]
  (let [res (client/get url {:as :byte-array})]
    (if (= (:status res) 200)
      (:body res))))

(defn- retrieve-images [content]
  (for [[_ img] (re-seq #"a href=\"([^\"]+)\"" content)
        :when (re-find #"\.je?pg$" img)]
    img))

(defn- download-images [url]
  (let [content (get-content url)]
    (when content
      (for [image (retrieve-images content)]
        (do
          (println "Download " image)
          (let [name (.getName (as-file image))
                image-content (get-content-as-byte-array image)]
            (when image-content
              (with-open [w (output-stream name)]
                (.write w image-content)))))))))

(defn -main
  [& urls]
  (for [url urls]
    (download-images url)))
