# Importing libraries
import time
import hashlib
import scrapy
import requests

from urllib import request
from urllib.request import urlopen, Request
from cgitb import html
from urllib.request import Request, urlopen
from bs4 import BeautifulSoup 
 

class WebSpider(scrapy.Spider):
    name = "CCL"

    start_urls = [ 
        'https://www.ecfr.gov/current/title-15/subtitle-B/chapter-VII/subchapter-C/part-774' 
    ]

    user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1"

    def parse(self, response):

        def addParagraphWithSiblings(firstParagraph, siblingsBetweenList):
            htmlString = ""
            htmlString = htmlString + "<html><body>"
            htmlString = htmlString + str(firstParagraph)
            for Element in siblingsBetweenList: 
                htmlString = htmlString + str(Element)
            htmlString = htmlString + "</body></html>"
            with open('content.html', 'w', encoding='UTF-8') as f:
                f.write(str(htmlString))    


        my_url = 'https://www.ecfr.gov/current/title-15/subtitle-B/chapter-VII/subchapter-C/part-774'

        #adding headers
        hdr = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36'}

        #creating URL   
        req = Request(my_url,headers=hdr)

        # to perform a GET request and load the
        webSiteContent = urlopen(req).read()

        # to create the initial hash
        currentHash = hashlib.sha224(webSiteContent).hexdigest()
        #print("running")
        time.sleep(10)

        #scraping the HTML content from website 
        soup = BeautifulSoup(webSiteContent, features='html.parser')
        firstParagraph = soup.find('p', {'class':'flush-paragraph-2'})
        allNextSiblings = firstParagraph.findNextSiblings()
        
        #Calling the function to add the content to the file
        addParagraphWithSiblings(firstParagraph, allNextSiblings)
    
        while True:
            try:
                webSiteContent = urlopen(req).read()
                
                # create a hash
                currentHash = hashlib.sha224(webSiteContent).hexdigest()
                
                # wait for 30 seconds
                time.sleep(30)
                
                webSiteContent = urlopen(req).read()

                # create a new hash
                newHash = hashlib.sha224(webSiteContent).hexdigest()
        
                # check if new hash is same as the previous hash
                if newHash == currentHash:
                    print("nothing changed")
                    continue
        
                # if something changed in the hashes
                else:
                    # notify
                    print("something changed")
        
                    # again read the website
                    webSiteContent = urlopen(req).read()
        
                    # create a hash
                    currentHash = hashlib.sha224(webSiteContent).hexdigest()

                    #scraping the updated HTML content from website 
                    soup = BeautifulSoup(webSiteContent, features='html.parser')
                    firstParagraph = soup.find('p', {'class':'flush-paragraph-2'})
                    allNextSiblings = firstParagraph.findNextSiblings()

                    #Calling the function to add the updated content to the file
                    addParagraphWithSiblings(firstParagraph, allNextSiblings)
    
                    # wait for 10 seconds
                    time.sleep(10)
                    print('file saved')
                    continue
                    
            # To handle exceptions
            except Exception as e:
                print("error")
        