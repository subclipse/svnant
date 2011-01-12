all: DOMXIncluder SAXXIncluder JDOMXIncluder docs

SAXXIncluder: src/com/macfaq/xml/SAXXIncluder.java
	javac -d bin -sourcepath src src/com/macfaq/xml/SAXXIncluder.java

DOMXIncluder: src/com/macfaq/xml/DOMXIncluder.java
	javac -d bin -sourcepath src src/com/macfaq/xml/DOMXIncluder.java

JDOMXIncluder: src/com/macfaq/xml/JDOMXIncluder.java EncodingHeuristics
	javac -d bin -sourcepath src src/com/macfaq/xml/JDOMXIncluder.java

EncodingHeuristics: src/com/macfaq/xml/EncodingHeuristics.java
	javac -d bin -sourcepath src src/com/macfaq/xml/EncodingHeuristics.java

saxtest:
	java com.macfaq.xml.SAXXIncluder file:/C:/XMLJAVA/masterbook.xml
    
domtest:
	java com.macfaq.xml.DOMXIncluder file:/C:/XMLJAVA/masterbook.xml testout.xml  

test:
	java com.macfaq.xml.DOMXIncluder test.xml 

roottest:
	java com.macfaq.xml.DOMXIncluder roottest.xml 

crimsontest:
	java -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl com.macfaq.xml.DOMXIncluder test.xml 

gnutest:
	java -Djavax.xml.parsers.DocumentBuilderFactory=gnu.xml.dom.JAXPFactory com.macfaq.xml.DOMXIncluder test.xml 

docs:
	javadoc -d docs -sourcepath src com.macfaq.xml   
        
