(ns piper.template
  (:require [clojure.xml :as xml]
            [instaparse.core :as insta])
  (:import (java.io ByteArrayInputStream)))

(def template "<html>
  <head>
    <script type=\"fragment\" src=\"http://assets.domain.com\"></script>
  </head>
  <body>
    <fragment src=\"http://header.domain.com\"></fragment>
    <fragment src=\"http://content.domain.com\" primary></fragment>
    <fragment src=\"http://footer.domain.com\" async></fragment>
  </body>
</html>")

(def template-1 "<!doctype html>
<html>
  <head>
    <meta charset=\"utf-8\">
    <link href=\"data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABmJLR0T///////8JWPfcAAAACXBIWXMAAABIAAAASABGyWs+AAAAF0lEQVRIx2NgGAWjYBSMglEwCkbBSAcACBAAAeaR9cIAAAAASUVORK5CYII=\" rel=\"icon\" type=\"image/x-icon\" />
    <script type=\"slot\" name=\"head\"></script>
    <script>
    define('word', function () {
                                // Example dependency for the fragments
                                return 'initialised';
                                });
    </script>
  </head>
  <body>
    <slot name=\"body-start\"></slot>
    <div>
      <script>
        // this tests that the pipe functionality may
        // not be broken by script after the placeholder scripts
        document.body.appendChild(document.createElement('script'));
      </script>
      <h2>Fragment 1:</h2>
      <fragment src=\"http://localhost:8088\" primary fallback-src=\"http://localhost:8081\"></fragment>
      <h2>Fragment 2:</h2>
      <fragment async src=\"http://localhost:8082\"></fragment>
      <div>All done!</div>
    </div>
  </body>
</html>")

(xml/parse (ByteArrayInputStream. (.getBytes template)))


(def parser (insta/parser
              "
              tags             = (tag | close-tag | closed-tag | text)*
              close-tag        = <'</'> name <'>'>
              closed-tag       = <'<'> name arg* <'/>'>
              tag              = <'<'> name arg* <'>'>
              arg              = arg-with-value | arg-no-value
              arg-with-value   = name <'='> arg-value
              arg-no-value     = name
              arg-value        = <quote> #'[^\"]*' <quote>
              quote            = #'\"'
              name             = #'[a-zA-Z0-9!-]+'
              text             = #'[^<]*'
              "
              :auto-whitespace :standard))

(comment
  (parser "<hello>")
  (parser "<hello src=\"hello\"/>")
  (parser "<hello src=\"hello\">")
  (parser "<hello>world >>> xx bb</hello>")
  (parser template)
  (parser template-1))
