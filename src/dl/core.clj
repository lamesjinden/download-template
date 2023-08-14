#!/usr/bin/env bb

(ns dl.core
  (:require [clojure.java.io :as io]
            #_[clojure.string :as str]
            [babashka.fs :as fs]
            [babashka.http-client :as http]
            [taoensso.timbre :as timbre :refer [info infof debug debugf error errorf trace tracef]])
  #_(:import [java.net URLEncoder]))

(def continueOnError true)

(def sources [{:url "https://path/to/subject"
               :out "./path/to/destination.file"}])

#_(def cookie "")
(def default-http-options {:as :stream
                           #_:headers #_["cookie" cookie]})

(defn download [url options]
  (http/get url options))

(defn download-file [^String url ^String destination]
  (let [options default-http-options
        response (download url options)
        source-stream (:body response)
        destination-part (str destination ".part")]
    (try
      (with-open [destination-stream (->> destination-part
                                          io/as-file
                                          io/output-stream)]
        (io/copy source-stream destination-stream))
      (fs/move destination-part destination {:replace-existing true})
      (finally
        (fs/delete-if-exists destination-part)))))

(defn run []
  (info "downloading...")
  (let [downloadables (->> sources
                           ;; note - transform sources here
                           (filter #(when % %))
                           (map identity))
        download-fn (fn [url out]
                      (download-file url out)
                      (info "  done"))]
    (doseq [{:keys [url out]} downloadables]
      (info "  getting" (.toString out) "from" url)
      (if continueOnError
        (try
          (download-fn url out)
          (catch Exception e
            (error "  failed to get" (.toString out) "\n  " (.getMessage e))
            (trace "  " (.toString e))))
        (download-fn url out))))
  (info "all done"))

;; $> bb -m dl.core
;; or
;; $> ./src/dl/core.clj
(defn -main [& _args]
  (timbre/set-level! :info)
  (run))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment

  (-main)

  ;
  )