<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="marc xs" version="1.0">
    <xsl:output indent="yes" encoding="UTF-8"/>
    <xsl:template name="validateDatafield">
        <xsl:param name="isObselete" select="false()"/>
        <xsl:param name="sCodesNR"/>
        <xsl:param name="sCodesR"/>
        <xsl:param name="i1Values"/>
        <xsl:param name="i2Values"/>
        <xsl:if test="$isObselete=true()">
            <warning type="ObsoleteTag">
                <xsl:call-template name="controlNumber"/>
                <tag>
                    <xsl:value-of select="@tag"/>
                </tag>
            </warning>
        </xsl:if>
        <xsl:call-template name="checkNRSubfields">
            <xsl:with-param name="sCodesNR" select="$sCodesNR"/>
        </xsl:call-template>
        <xsl:call-template name="validateSubfields">
            <xsl:with-param name="sCodes" select="concat($sCodesR,$sCodesNR)"/>
        </xsl:call-template>
        <xsl:call-template name="validateIndicator1">
            <xsl:with-param name="iValues" select="$i1Values"/>
        </xsl:call-template>
        <xsl:call-template name="validateIndicator2">
            <xsl:with-param name="iValues" select="$i2Values"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="checkNRSubfields">
        <xsl:param name="sCodesNR"/>
        <xsl:if test="$sCodesNR">
            <xsl:if test="count(marc:subfield[@code=substring($sCodesNR,1,1)])&gt;1">
                <error type="NonRepeatableSubFieldRepeats">
                    <xsl:call-template name="controlNumber"/>
                    <tag>
                        <xsl:value-of select="@tag"/>
                    </tag>
                    <code>
                        <xsl:value-of select="substring($sCodesNR,1,1)"/>
                    </code>
                </error>
            </xsl:if> <xsl:call-template name="checkNRSubfields">
            <xsl:with-param name="sCodesNR" select="substring($sCodesNR,2)"/>
        </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <xsl:template name="validateSubfields">
        <xsl:param name="sCodes"/>
        <xsl:for-each select="marc:subfield">
            <xsl:if test="not(contains($sCodes, @code))">
                <error type="InvalidSubfieldCode">
                    <xsl:call-template name="controlNumber"/>
                    <tag>
                        <xsl:value-of select="../@tag"/>
                    </tag>
                    <code>
                        <xsl:value-of select="@code"/>
                    </code>
                </error>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="validateIndicator1">
        <xsl:param name="iValues"/>
        <xsl:if test="not(contains($iValues,@ind1))">
            <error type="InvalidIndicator">
                <xsl:call-template name="controlNumber"/>
                <tag>
                    <xsl:value-of select="@tag"/>
                </tag>
                <ind1>
                    <xsl:value-of select="@ind1"/>
                </ind1>
            </error>
        </xsl:if>
    </xsl:template>
    <xsl:template name="validateIndicator2">
        <xsl:param name="iValues"/>
        <xsl:if test="not(contains($iValues,@ind2))">
            <error type="InvalidIndicator">
                <xsl:call-template name="controlNumber"/>
                <tag>
                    <xsl:value-of select="@tag"/>
                </tag>
                <ind2>
                    <xsl:value-of select="@ind2"/>
                </ind2>
            </error>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/">
        <validationReport>
            <xsl:if test="marc:collection">
                <xsl:apply-templates select="marc:collection/marc:record"/>
            </xsl:if>
            <xsl:if test="marc:record">
                <xsl:apply-templates select="marc:record"/>
            </xsl:if>
        </validationReport>
    </xsl:template>

    <xsl:template match="marc:record">
        <xsl:if test="count(marc:controlfield[@tag=001]) != 1">
            <error type="MandatoryNonRepeatable">
                <tag>001</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:controlfield[@tag=003]) &gt; 1">
            <error type="NonRepeatable">
                <tag>003</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:controlfield[@tag=005]) &gt; 1">
            <error type="NonRepeatable">
                <tag>005</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:controlfield[@tag=006]) &gt; 1">
            <error type="NonRepeatable">
                <tag>006</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:controlfield[@tag=008]) != 1">
            <error type="MandatoryNonRepeatable">
                <tag>008</tag>
            </error>
        </xsl:if>

        <!-- -->

        <xsl:if test="count(marc:datafield[@tag=010])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>010</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=018])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>018</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=036])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>036</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=038])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>038</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=040])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>040</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=042])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>042</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=043])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>043</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=044])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>044</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=045])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>045</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=066])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>066</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=100])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>100</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=110])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>110</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=111])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>111</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=130])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>130</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=240])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>240</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=243])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>243</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=245]) != 1">
            <error type="MandatoryNonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>245</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=254])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>254</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=256])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>256</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=263])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>263</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=306])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>306</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=310])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>310</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=357])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>357</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=384])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>384</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=507])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>507</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=514])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>514</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=841])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>841</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=842])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>842</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=844])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>844</tag>
            </error>
        </xsl:if>
        <xsl:if test="count(marc:datafield[@tag=882])&gt;1">
            <error type="NonRepeatable">
                <xsl:call-template name="controlNumber"/>
                <tag>882</tag>
            </error>
        </xsl:if>

        <!-- -->
        <xsl:apply-templates select="marc:datafield"/>
        <xsl:apply-templates select="marc:controlfield"/>
    </xsl:template>
    <xsl:template match="marc:controlfield[@tag=001]">
    </xsl:template>
    <xsl:template match="marc:controlfield[@tag=003]">
    </xsl:template>
    <xsl:template match="marc:controlfield[@tag=005]">
    </xsl:template>
    <xsl:template match="marc:controlfield[@tag=008]">
    </xsl:template>
    <!-- -->

    <xsl:template match="marc:datafield[@tag=010]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=013]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">def8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abc6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=015]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">aqz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=016]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a2</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 7</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=017]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">az8</xsl:with-param>
            <xsl:with-param name="sCodesNR">bdi26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=018]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=020]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">qz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ac6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=022]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">myz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">al26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=024]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">qz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">acd26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123478</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=025]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=026]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">d58</xsl:with-param>
            <xsl:with-param name="sCodesNR">abce26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=027]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">qz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=028]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">q8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=030]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=031]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dqstuyz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcegmnopr26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=032]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=033]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcp028</xsl:with-param>
            <xsl:with-param name="sCodesNR">36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 012</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=034]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bchst08</xsl:with-param>
            <xsl:with-param name="sCodesNR">adefgjkmnprxyz236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">013</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=035]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=036]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=037]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">cfgn8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=038]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=040]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">de8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abc6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=041]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abdefghjkmn8</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 7</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=042]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=043]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abc028</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=044]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abc28</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=045]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abc8</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=046]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdejklmnop26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=047]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">2</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 7</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=048]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab8</xsl:with-param>
            <xsl:with-param name="sCodesNR">2</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 7</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=050]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">b36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">04</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=051]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abc</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=052]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bd8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 17</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=055]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=060]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">b</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">04</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=061]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">bc</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=066]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">c</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=070]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">b</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=071]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ac8</xsl:with-param>
            <xsl:with-param name="sCodesNR">b</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=072]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">x8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">07</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=074]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=080]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">x8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=082]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">bmq26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">017</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 04</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=083]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">acyz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">mq26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">017</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=084]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">bq26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=085]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcfrstuvwyz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=086]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=088]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=100]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">cejknp048</xsl:with-param>
            <xsl:with-param name="sCodesNR">abdfglqtu6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">013</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=110]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bdeknp048</xsl:with-param>
            <xsl:with-param name="sCodesNR">acfgltu6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=111]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ejknp048</xsl:with-param>
            <xsl:with-param name="sCodesNR">acdfglqtu6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=130]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dkmnp08</xsl:with-param>
            <xsl:with-param name="sCodesNR">afghlorst6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123456789</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=210]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">28</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 0</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=222]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=240]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dkmnp08</xsl:with-param>
            <xsl:with-param name="sCodesNR">afghlors6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=242]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">np8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abchy6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=243]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dkmnp8</xsl:with-param>
            <xsl:with-param name="sCodesNR">afghlors6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=245]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">knp8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcfghs6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=246]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">np8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abfghi56</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 012345678</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=247]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">np8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abfghx6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=250]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=254]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=255]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefg6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=256]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=257]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=258]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=260]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcefg8</xsl:with-param>
            <xsl:with-param name="sCodesNR">36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 23</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=263]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=264]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abc8</xsl:with-param>
            <xsl:with-param name="sCodesNR">36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 23</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=270]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ajklmnpqrz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">bcdefghi6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 12</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 07</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=300]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">acfg8</xsl:with-param>
            <xsl:with-param name="sCodesNR">be36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=306]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=307]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=310]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=321]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=336]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab8</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=337]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab8</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=338]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab8</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=340]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefhijkmno08</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=342]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ef8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdghijklmnopqrstuvw26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">012345678</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=343]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghi6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=344]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefgh08</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=345]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab08</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=346]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab08</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=347]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdef08</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=351]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab8</xsl:with-param>
            <xsl:with-param name="sCodesNR">c36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=352]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bc8</xsl:with-param>
            <xsl:with-param name="sCodesNR">adefgiq6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=355]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcj8</xsl:with-param>
            <xsl:with-param name="sCodesNR">adefgh6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123458</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=357]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcg8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=362]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">az6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=363]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmuv68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=365]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijkm26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=366]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefgjkm26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=377]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">al8</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 7</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=380]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a08</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=381]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">auv08</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=382]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abdnpsv08</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=383]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abc8</xsl:with-param>
            <xsl:with-param name="sCodesNR">de26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=384]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=385]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab08</xsl:with-param>
            <xsl:with-param name="sCodesNR">mn236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=386]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab08</xsl:with-param>
            <xsl:with-param name="sCodesNR">mn236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=490]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">avx8</xsl:with-param>
            <xsl:with-param name="sCodesNR">l36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=500]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=501]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a56</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=502]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">go8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcd6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=504]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=505]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">grtu8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0128</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 0</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=506]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdefu8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a2356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=507]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=508]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=510]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcx36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01234</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=511]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=513]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=514]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcghjkuz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">adefim6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=515]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=516]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=518]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dop028</xsl:with-param>
            <xsl:with-param name="sCodesNR">a36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=520]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abc236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012348</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=521]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">b36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012348</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=522]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=524]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=525]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=526]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdi56</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">08</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=530]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcd36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=533]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcfmn8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ade3576</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=534]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">fknoxz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcelmpt36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=535]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcd8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ag36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">12</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=536]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdefgh8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=538]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u58</xsl:with-param>
            <xsl:with-param name="sCodesNR">ai36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=540]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcd356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=541]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">no8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefh356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=542]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">defhknpu8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcgijlmoqrs36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=544]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcden8</xsl:with-param>
            <xsl:with-param name="sCodesNR">36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=545]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=546]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">b8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=547]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=550]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=552]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">efopuz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdghijklmn6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=555]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bu8</xsl:with-param>
            <xsl:with-param name="sCodesNR">acd36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 08</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=556]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=561]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=562]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcde8</xsl:with-param>
            <xsl:with-param name="sCodesNR">356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=563]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=565]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcde8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 08</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=567]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=580]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=581]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">z8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=583]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdefhijklnouxz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a2356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=584]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ab8</xsl:with-param>
            <xsl:with-param name="sCodesNR">356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=585]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=586]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 8</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=588]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a56</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=600]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">cejkmnpvxyz048</xsl:with-param>
            <xsl:with-param name="sCodesNR">abdfghloqrstu236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">013</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=610]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bdekmnpvxyz048</xsl:with-param>
            <xsl:with-param name="sCodesNR">acfghlorstu236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=611]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ejknpvxyz048</xsl:with-param>
            <xsl:with-param name="sCodesNR">acdfghlqstu236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=630]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dekmnpvxyz048</xsl:with-param>
            <xsl:with-param name="sCodesNR">afghlorst236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123456789</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=648]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">vxyz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">a236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=650]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">e4vxyz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcd236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=651]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">e4vxyz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">a236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=653]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 0123456</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=654]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcevyz048</xsl:with-param>
            <xsl:with-param name="sCodesNR">236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=655]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcvxyz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">a2356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 0</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=656]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">vxyz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">ak236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">7</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=657]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">vxyz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">a236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">7</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=658]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">b8</xsl:with-param>
            <xsl:with-param name="sCodesNR">acd26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=662]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">acefgh048</xsl:with-param>
            <xsl:with-param name="sCodesNR">bd26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=700]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ceijkmnp048</xsl:with-param>
            <xsl:with-param name="sCodesNR">abdfghloqrstux356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">013</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 2</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=710]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bdeikmnp048</xsl:with-param>
            <xsl:with-param name="sCodesNR">acfghlorstux356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 2</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=711]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">eijknp048</xsl:with-param>
            <xsl:with-param name="sCodesNR">acdfghlqstux356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 2</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=720]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">e48</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 12</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=730]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dikmnp08</xsl:with-param>
            <xsl:with-param name="sCodesNR">afghlorstx356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123456789</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 2</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=740]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">np8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ah56</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123456789</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 2</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=751]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">e048</xsl:with-param>
            <xsl:with-param name="sCodesNR">a236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=752]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">acfgh08</xsl:with-param>
            <xsl:with-param name="sCodesNR">bd26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=753]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abc6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=754]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">acdxz08</xsl:with-param>
            <xsl:with-param name="sCodesNR">26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=760]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ginow48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=762]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ginow48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=765]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=767]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=770]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=772]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 08</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=773]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abdhmpqstuxy367</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=774]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=775]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=776]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=777]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknow48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=780]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">01234567</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=785]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">012345678</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=786]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhjmpstuvxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=787]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">giknorwz48</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdhmstuxy67</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 8</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=800]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">cejkmnpw0458</xsl:with-param>
            <xsl:with-param name="sCodesNR">abdfghloqrstuvx367</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">013</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=810]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bdekmnpw0458</xsl:with-param>
            <xsl:with-param name="sCodesNR">acfghlorstuvx367</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=811]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">ejknpw0458</xsl:with-param>
            <xsl:with-param name="sCodesNR">acdfghlqstuvx367</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=830]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">dkmnpw058</xsl:with-param>
            <xsl:with-param name="sCodesNR">afghlorstvx367</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123456789</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=841]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR"></xsl:with-param>
            <xsl:with-param name="sCodesNR">abe</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=842]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=843]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcfmn8</xsl:with-param>
            <xsl:with-param name="sCodesNR">ade3567</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=844]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=845]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">u8</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcd356</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=850]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">a8</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=852]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdefgikmsuxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">ahjlnpqt2368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012345678</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 012</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=856]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdfimstuvwxyz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">hjklnopqr236</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 012347</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 0128</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=882]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">aiw8</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=883]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">w08</xsl:with-param>
            <xsl:with-param name="sCodesNR">acdqxu</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 01</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=887]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR"></xsl:with-param>
            <xsl:with-param name="sCodesNR">a2</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=853]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">uvyz28</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnoptwx36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=854]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">uvyz28</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnoptwx36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=855]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">uvyz28</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnoptwx36</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=863]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">sxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpqtw68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01234</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=864]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">sxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnopqtw68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01234</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=865]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">svxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnopqtw68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01234</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=866]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0127</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=867]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0127</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=868]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0127</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=876]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdehjlprxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">at368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=877]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdehjlprxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">at368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=878]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdehjlprxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">at368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=880]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz012345789</xsl:with-param>
            <xsl:with-param name="sCodesNR">6</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=886]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">cdefghijklmnopqrstuvwxyz013456789</xsl:with-param>
            <xsl:with-param name="sCodesNR">ab2</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">012</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=090]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=099]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=590]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=591]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=592]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=593]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=594]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=595]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=596]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=597]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=598]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=599]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=936]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=964]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">abcdefghijklmnopqrstuvwxyz0123456789</xsl:with-param>
            <xsl:with-param name="sCodesNR"></xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 1234567890</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 1234567890</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- -->
    <xsl:template match="marc:datafield[starts-with(@tag,'09')]">
    </xsl:template>
    <xsl:template match="marc:datafield[starts-with(@tag,'59')]">
    </xsl:template>
    <xsl:template match="marc:datafield[starts-with(@tag,'9')]">
    </xsl:template>
    <xsl:template match="marc:datafield[starts-with(@tag,'880')]">
    </xsl:template>
    <xsl:template match="marc:datafield[starts-with(@tag,'886')]">
    </xsl:template>

    <xsl:template match="marc:datafield[@tag=853]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">uvyzo2</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpwxt368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=854]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">uvyzo2</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpwxt368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve">0123</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0123</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=855]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">uvyzo2</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpwxt368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="marc:datafield[@tag=863]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">osxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpqtw68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01234</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=864]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">osxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpqtw68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 01234</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=865]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">osvxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">abcdefghijklmnpqtw68</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 45</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> 13</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=866]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0127</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=867]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0127</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=868]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">xz8</xsl:with-param>
            <xsl:with-param name="sCodesNR">a26</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> 345</xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve">0127</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=876]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdehjlprxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">at368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=877]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdehjlprxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">at368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="marc:datafield[@tag=878]">
        <xsl:call-template name="validateDatafield">
            <xsl:with-param name="sCodesR">bcdehjlprxz</xsl:with-param>
            <xsl:with-param name="sCodesNR">at368</xsl:with-param>
            <xsl:with-param name="i1Values" xml:space="preserve"> </xsl:with-param>
      <xsl:with-param name="i2Values" xml:space="preserve"> </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="marc:datafield">
        <warning type="UnknownTag">
            <xsl:call-template name="controlNumber"/>
            <tag>
                <xsl:value-of select="@tag"/>
            </tag>
        </warning>
    </xsl:template>
    <xsl:template name="controlNumber">
        <xsl:if test="../marc:controlfield[@tag=001]">
            <xsl:attribute name="controlNumber">
                <xsl:value-of select="../marc:controlfield[@tag=001]"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>