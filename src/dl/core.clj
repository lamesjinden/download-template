#!/usr/bin/env bb

(ns dl.core
  (:require [clojure.java.io :as io]
            #_[clojure.string :as str]
            #_[babashka.fs :as fs]
            [babashka.http-client :as http])
  #_(:import [java.net URLEncoder]))

(def sources [{:url "https://path/to/subject"
               :out "./path/to/destination.file"}])

#_(def cookie "")
(def default-http-options {:as :stream
                           #_:headers #_["cookie" cookie]})

(defn download [url options]
  (http/get url options))

(defn download-file [url destination]
  (let [options default-http-options
        response (download url options)
        source-stream (:body response)]
    (with-open [destination-stream (->> destination
                                        io/as-file
                                        io/output-stream)]
      (io/copy
       source-stream
       destination-stream))))

(defn run []
  (println "downloading...")
  (let [downloadables (->> sources
                           (filter #(when % %))
                           (map identity))]
    (doseq [{:keys [url out] :as _downloadable} downloadables]
      (println "  " url)
      (download-file url out)
      (println "  done")))
  (println "all done"))

;; $> bb -m dl.core
;; or
;; $> ./src/dl/core.clj
(defn -main [& _args]
  (run))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment

  (-main)

  ;
  )