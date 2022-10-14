package com.me.vizitka

import java.util.regex.Pattern

class Data {
    private static String data
    private static String dataHtml
    static byte[] foto
    static String rootTop
    static String rootBottom
    static String formTop
    static String formBottom

    static String getData() {
        data
    }

    static void setData(String data) {
        this.data = data
        println "TODO, for some reason this never gets called" //TODO
        dataToHtml()
    }

    //store data on disk
    static void storeData() {
        new File('private/data.txt').text = data
    }

    static String getDataHtml() {
        dataHtml
    }

    private static void setDataHtml(String dataHtml) {
        this.dataHtml = dataHtml
    }

    public static init() {
        data = new File('private/data.txt').text
        dataToHtml()
        foto = new File('public/foto.jpg').bytes
        String root = new File('private/root.html').text
        var temp = root.split(Pattern.quote("{{datahere}}"))
        rootTop = temp[0]
        rootBottom = temp[1]
        String form = new File('private/form.html').text
        temp = form.split(Pattern.quote("{{datahere}}"))
        formTop = temp[0]
        formBottom = temp[1]
    }

    private static class LineParser {
        private String line
        private int pointText, pointLink, pointLinkEnd, lineEnd

        private enum NextIs {TEXT, LINK}
        private NextIs nextIs

        void init(String line) {
            this.line = line
            nextIs = NextIs.TEXT
            pointText = 0
            pointLink = 0
            pointLinkEnd = 0
            lineEnd = line.length()
        }

        //returns empty string when runs out of things to send
        //TODO, maybe use regex here?
        String next() {
            while (true) {
                if (nextIs == NextIs.TEXT) {
                    if (pointLink == lineEnd) return ""
                    pointLink = line.indexOf("http", pointLink)
                    if (pointLink == -1) {
                        pointLink = lineEnd
                        return line.substring(pointText, lineEnd)
                    }
                    int point = pointLink + "http".length()
                    boolean yepItsaLink = false
                    if (line.regionMatches(point, "://", 0, "://".length())) {
                        point += "://".length()
                        yepItsaLink = true
                    } else if (line.regionMatches(point, "s://", 0, "s://".length())) {
                        point += "s://".length()
                        yepItsaLink = true
                    }
                    if (!yepItsaLink) {
                        pointLink = point
                        continue
                    }
                    point = line.indexOf(" ", point)
                    if (point == -1) {
                        pointLinkEnd = lineEnd
                    } else {
                        pointLinkEnd = point
                    }
                    nextIs = NextIs.LINK
                    if (pointText != pointLink) {
                        return line.substring(pointText, pointLink)
                    } else {
                        continue
                    }
                } else if (nextIs == NextIs.LINK) {
                    String link = line.substring(pointLink, pointLinkEnd)
                    pointLink = pointText = pointLinkEnd
                    nextIs = NextIs.TEXT
                    return '<a href="' + link + '">' + link + '</a>'
                }
            }
        }
    }

    static dataToHtml() {
        var lineParser = new LineParser()
        String result = ""
        for (String line : data.split("\r\n")) { //I'd use "\n", but multipart comes with \r\n anyway
            lineParser.init(line)
            String next
            while (next = lineParser.next()) {
                result += next
            }
            result += "<br/>\n"
        }
        dataHtml = result
    }
}
