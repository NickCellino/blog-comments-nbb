(ns comments.express
  (:require ["express$default" :as express]
            ["http" :as http]
            [cljs-bean.core :as bean]
            [comments.html :as html]))

(defn create-comment
  [req res]
  (.log js/console (.-body req))
  (.send res "adding a comment"))

(defn parse-comment-body
  [js-body]
  (let [body (bean/->clj js-body)]
    (cond-> {}
      (:author body) (assoc :author (:author body))
      (:message body) (assoc :message (:message body)))))

(defn create-app
  [add-comment list-comments]
  (let [app (express)]
    (.use app (.urlencoded express #js {:extended true}))
    (.get app "/" (fn [req res]
                    (.send res "<h1>Hello world!</h1>")))
    (.get app "/comments" (fn [req res]
                            (let [comments (list-comments "clojure-bandits")]
                              (.send res (bean/->js (html/serialize-comment-list comments))))))
    (.post app "/comments" (fn [req res]
                             (let [comment-input (parse-comment-body (.-body req))]
                               (add-comment comment-input)
                               (.send res (bean/->js (html/serialize-comment comment-input))))))
    (.get app "/comments-form" (fn [req res]
                                 (.send res html/comments-form)))
    app))

(defn start-server
  [app port callback]
  (let [server (.createServer http app)]
    (.listen server port callback)))

(defn stop-server
  [server]
  (.close server))
