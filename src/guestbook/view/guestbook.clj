(ns guestbook.view.guestbook
  (:require [guestbook.view.bootstrap :as bs]))

(defn home [data]
  (bs/layout
   [:div
    [:h1 "Welcome Home"]

    (bs/table ["name" "message"]
              (for [message (data :messages)]
                [:tr
                 [:td (message :name)]
                 [:td (message :message)]]))
    (bs/form "/"
             data
             [:div
              (bs/text-input "Name" :name data)
              (bs/text-input "Message" :message data)
              (bs/submit-btn "Add Message")])]))
