<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="2.0" 
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:output 
    method="html" 
    encoding="UTF-8"
    standalone="yes"
    version="1.0"
    doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" 
    indent="yes"
  />

  <xsl:template name="head">
    <xsl:param name="title"/>
    <head>
      <title><xsl:value-of select="$title"/></title>
      <meta http-equiv="Content-Language" content="en-us"/>
      <link rel="stylesheet" type="text/css" href="http://tortoisesvn.tigris.org/branding/css/print.css" media="print"/>
      <style type="text/css">
        /* <![CDATA[ */
            @import "http://subclipse.tigris.org/branding/css/tigris.css";
            @import "http://subclipse.tigris.org/branding/css/inst.css";
        /* ]]> */
      </style>
      <style type="text/css">
        table {
          border: 1pt solid gray;
          border-collapse: collapse;
          width: 70%;
        }
        tr, td {
          border: 1pt solid gray;
        }
        tr:hover {
          background-color: rgb(93%,93%,93%);
        }
        td {
          padding: 2pt;
        }
        tr {
          width: 100%;
        }
        h1 {
          font-size: 200%; text-transform: lowercase; letter-spacing: 3px;
          margin-bottom: 1em; 
          padding: 0.66em 0 0.33em 1em;
          /* background: rgb(85%,85%,70%); */
        }
        h2 {
          background: rgb(85%,85%,70%);
          margin-bottom: 1em; 
        }
        h3 {
          margin-bottom: 1em; 
          border:1px solid rgb(85%,85%,70%);
          color: rgb(55%,55%,40%);
          padding: 0.66em 0 0.33em 1em;
          background-color: #f3f1f4;
        }
        .sample {
          font-family: Courier, "Courier New", monospace;
          background-color: #f3f1f4;
          margin-left: 10px;
          margin-right: 10px;
          padding-top: 3px;
          padding-bottom: 3px;
          padding-left: 3px;
          padding-right: 3px;
          border:1px dashed black;
          width: 65%;
        }
        .firstcol {
          width: 15em;
          max-width: 15em;
        }
        .lastcol {
          width: 6em;
          max-width: 6em;
        }
        p.remark {
          margin-left: 2em;
          padding-left: 1em;
          padding-top: 2pt;
          padding-bottom: 2pt;
          padding-right: 2pt;
          border-style: solid;
          border-color: red;
          border-left-width: 4em;
          border-top-width: 1pt;
          border-right-width: 1pt;
          border-bottom-width: 1pt;
        }
        table.attributes {
          margin-bottom: 2em;
        } 
        table.nestedelements tr td.firstcol {
          text-style: bold;
          width: 10em;
        }
        tr.invisiblerow {
          border: 0pt solid black;
          padding-top: 3pt;
          padding-bottom: 3pt;
        }
      </style> 
    </head>
  </xsl:template>
  
  <xsl:template match="/">
    <xsl:apply-templates select="mdoc"/>
  </xsl:template>
  
  <xsl:template match="mdoc">
    <xsl:variable name="title"><xsl:value-of select="@title"/></xsl:variable>
    <html>
      <xsl:call-template name="head">
        <xsl:with-param name="title" select="$title"/>
      </xsl:call-template>
      <body lang="en-US" dir="ltr">
        <xsl:if test="@header = 'true'">
          <h1>&lt;<xsl:value-of select="@name"/>&gt;</h1>
        </xsl:if>
        <xsl:apply-templates select="msection"/>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="nestedelements">
    <xsl:variable name="title">
      <xsl:choose>
        <xsl:when test="string-length(@title) &gt; 0"><xsl:value-of select="@title"/></xsl:when>
        <xsl:otherwise>Nested elements</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="count(nestedelement) &gt; 0">
      <table class="nestedelements">
        <tr>
          <th colspan="2"><xsl:value-of select="$title"/></th>
        </tr>
        <tr>
          <th>Element</th>
          <th>Description</th>
        </tr>
        <xsl:apply-templates select="nestedelement"/>
      </table>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="nestedelement">
    <tr>
      <td class="firstcol"><xsl:value-of select="@name"/></td>
      <td><p><xsl:apply-templates select="doc"/></p></td>
    </tr>
  </xsl:template>
  
  <xsl:template match="attributes">
    <xsl:if test="count(attribute) &gt; 0">
      <table class="attributes">
        <tr>
          <th class="firstcol">Attribute</th>
          <th>Description</th>
          <th class="lastcol">Required</th>
        </tr>
        <xsl:apply-templates select="attribute|grouped"/>
      </table>
    </xsl:if>
  </xsl:template>

  <xsl:template match="attribute">
    <tr>
      <td class="firstcol"><xsl:value-of select="@name"/></td>
      <td><p><xsl:apply-templates select="doc"/></p>
      <xsl:if test="string-length(@default) &gt; 0">
        <br/>
        Default: <xsl:value-of select="@default"/> 
      </xsl:if>
      </td>
      <td class="lastcol">
        <xsl:choose>
          <xsl:when test="@required = 'true'">Yes</xsl:when>
          <xsl:when test="@required = 'false'">No</xsl:when>
          <xsl:otherwise><xsl:value-of select="@required"/></xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="grouped">
    <xsl:variable name="attrcount"><xsl:value-of select="count(attribute)"/></xsl:variable>
    <xsl:for-each select="attribute">
      <tr>
        <td class="firstcol"><xsl:value-of select="@name"/></td>
        <td><p><xsl:apply-templates select="doc"/></p>
        <xsl:if test="string-length(@default) &gt; 0">
          <br/>
          Default: <xsl:value-of select="@default"/> 
        </xsl:if>
        </td>
        <xsl:if test="position() = 1">
          <td class="lastcol"><xsl:attribute name="rowspan"><xsl:value-of select="$attrcount"/></xsl:attribute>One of these</td>
        </xsl:if>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="doc" mode="table">
    <xsl:call-template name="doc"/>
  </xsl:template>
  
  <xsl:template match="doc" name="doc">
    <xsl:for-each select="node()">
      <xsl:choose>
        <xsl:when test="string-length(name()) &gt; 0">
          <xsl:apply-templates select="."/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="i"><i><xsl:call-template name="doc"/></i></xsl:template>
  <xsl:template match="b"><b><xsl:call-template name="doc"/></b></xsl:template>
  <xsl:template match="p"><p><xsl:call-template name="doc"/></p></xsl:template>
  <xsl:template match="em"><em><xsl:call-template name="doc"/></em></xsl:template>
  <xsl:template match="ul"><ul><xsl:call-template name="doc"/></ul></xsl:template>
  <xsl:template match="ol"><ol><xsl:call-template name="doc"/></ol></xsl:template>
  <xsl:template match="li"><li><xsl:call-template name="doc"/></li></xsl:template>
  <xsl:template match="table"><table><xsl:call-template name="doc"/></table></xsl:template>
  <xsl:template match="tr"><tr><xsl:call-template name="doc"/></tr></xsl:template>
  <xsl:template match="td"><td><xsl:call-template name="doc"/></td></xsl:template>
  <xsl:template match="code"><code><xsl:call-template name="doc"/></code></xsl:template>
  <xsl:template match="br"><br/></xsl:template>
  
  <xsl:template match="a">
    <a><xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute><xsl:call-template name="doc"/></a>
  </xsl:template>

  <xsl:template match="msection">
    <div class="h2">
      <xsl:attribute name="title"><xsl:value-of select="@title"/></xsl:attribute>
      <xsl:call-template name="generate_anchor"/>
      <h2><xsl:value-of select="@title"/></h2>
      <xsl:call-template name="generate_index_subsection"/>
      <xsl:call-template name="mcontent"/>
    </div>
  </xsl:template>

  <xsl:template name="generate_index_subsection">
    <xsl:if test="@indexing = 'true'">
      <xsl:if test="count(msubsection[@id != '']) &gt; 0">
        <table>
          <xsl:for-each select="msubsection[@id != '']">
            <xsl:if test="position() mod 5 = 1">
              <tr>
                <td><a><xsl:attribute name="href">#<xsl:value-of select="@id"/></xsl:attribute><xsl:value-of select="@title"/></a></td>
                <td><a><xsl:attribute name="href">#<xsl:value-of select="following-sibling::msubsection[@id != '']/@id"/></xsl:attribute><xsl:value-of select="following-sibling::msubsection[@id != '']/@title"/></a></td>
                <td><a><xsl:attribute name="href">#<xsl:value-of select="following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/@id"/></xsl:attribute><xsl:value-of select="following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/@title"/></a></td>
                <td><a><xsl:attribute name="href">#<xsl:value-of select="following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/@id"/></xsl:attribute><xsl:value-of select="following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/@title"/></a></td>
                <td><a><xsl:attribute name="href">#<xsl:value-of select="following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/@id"/></xsl:attribute><xsl:value-of select="following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/following-sibling::msubsection[@id != '']/@title"/></a></td>
              </tr>
            </xsl:if>
          </xsl:for-each> 
        </table>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="msubsection">
    <div class="h3">
      <xsl:attribute name="title"><xsl:value-of select="@title"/></xsl:attribute>
      <xsl:call-template name="generate_anchor"/>
      <h3><xsl:value-of select="@title"/></h3>
      <xsl:call-template name="mcontent"/>
    </div>
  </xsl:template>

  <xsl:template match="msubsubsection">
    <xsl:attribute name="title"><xsl:value-of select="@title"/></xsl:attribute>
    <xsl:call-template name="generate_anchor"/>
    <h4><xsl:value-of select="@title"/></h4>
    <xsl:call-template name="mcontent"/>
  </xsl:template>

  <xsl:template name="mcontent">
    <xsl:for-each select="node()">
      <xsl:choose>
        <xsl:when test="string-length(name()) &gt; 0">
          <xsl:apply-templates select="."/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="snippet">
    <xsl:call-template name="generate_anchor"/>
    <pre class="sample">
      <xsl:value-of select="text()"/>
    </pre>
  </xsl:template>

  <xsl:template match="remark">
    <p class="remark">
      <xsl:call-template name="mcontent"/>
    </p>
  </xsl:template>


  <xsl:template name="generate_anchor">
    <xsl:if test="string-length(@id) &gt; 0">
      <a style="visibility: hidden;"><xsl:attribute name="name"><xsl:value-of select="@id"/></xsl:attribute></a>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>