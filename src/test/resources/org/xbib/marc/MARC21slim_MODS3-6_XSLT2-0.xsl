<xsl:stylesheet xmlns="http://www.loc.gov/mods/v3"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:local="http://www.loc.org/namespace" exclude-result-prefixes="xlink marc" version="2.0">
	<!--	<xsl:include href="http://www.loc.gov/standards/marcxml/xslt/MARC21slimUtils.xsl"/>-->
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>
	<!-- 
		Maintenance note: For each revision, change the content of <recordInfo><recordOrigin> to reflect the new revision number.
		MARC21slim2MODS3-5 (Revision 2.31) 2016315
		
		Revision 2.31 - Added nameIdentifier to 700/710/711/100/110/111 $0 RE: MODS 3.6 - 2016/3/15 ws
		Revision 2.30 - Added @otherType for 7xx RE: MODS 3.6 - 2016/3/15 ws
		Revision 2.29 - Added <itemIdentifier> for 852$p and <itemIdentifier > with type="copy number" for 852$t RE: MODS 3.6 - 2016/3/15 ws
		Revision 2.28 - Added @valueURI="contents of $0" for 752/662 RE: MODS 3.6 - 2016/3/15 ws
		Revision 2.27 - Added @xml:space="preserve" to title/nonSort on 245 and 242 RE: MODS 3.6 - 2016/3/15 ws
		
		Revision 2.26 - Added test to prevent empty authority attribute for 047 with no subfield 2. - ws 2016/03/24
		Revision 2.25 - Added test to prevent empty authority attribute for 655 and use if ind2 if no subfield 2 is available. - ws 2016/03/24
		Revision 2.24 - Added test to prevent empty authority attribute for 336 with no subfield 2. - ws 2016/03/24
		
		Revision 2.23 - Added a xsl:when to deal with '#' and ' ' in $leader19 and $controlField008-18 - ws 2014/12/19
		Revision 2.22 - Bug 740[@ind2 !='2'] inncorrectly suppressing alternate titles. 
		Revision 2.21 - Add identifier type="WlCaITV" - ws 2014/11/20 
		Revision 2.20 - Add orginInfo/@eventType - ws 2014/11/20
		Revision 2.19 - Bug: Removed duplicate dateIssued value - ws 2014/11/20
		Revision 2.18 - Add @unit to extent - ws 2014/11/20
		Revision 2.17 - Refined 653 mapping - tmee 2013/06/10
		Revision 2.16 - Deleted mapping for 534 to note - tmee 2013/01/17
		Revision 2.15 - Added authority attributes for 337$2 and 338$2 - 2013/01/17
		Revision 2.14 - Removed type="publication" from 264 ind1 dateIssued subelement - 2012/12/20 tmee
		Revision 2.13 - Added 264 field, additional originInfo element - 2012/11/01
		Revision 2.12 - Updated stylesheet to accommodate RDA. Changes made to 047, 336, 337 and 338 fields.
		Revision 2.11 - Added test to 100/700 to only output name if now $t  - 2012/09/06 ws
		Revision 2.10 - Added subfield s to title field 245$s
		Revision 2.0 - Upgraded stylesheet to MODS 3.4 - 2012/06/25 ws
		Revision 2.0 - Upgraded XSLT 1.0 1.78 stylesheet to XSLT 2.0 - 2012/06/25 ws
			 			210 <titleInfo type="abbreviated"> added authority attribute from $2			
			 			100 <name> added attribute type generated from ind1 and strips punctuation from namePart
			 			111/711 <name type="conference"> added $n
			 			260 <orginInfo><publisher> removes beginning and ending brackets if present
			 			520 <abstract> added displayLabel attribute from ind1
			 			505 <tableOfContents> added displayLabel attribute from ind1
			 			008 <targetAudience authority="marctarget"> added
			 			521 <targetAudience> added displayLabel from ind1
						245 $c added element <note type="statement of responsibility">
						563 <note> added element 
						045 <subject><temporal> fixed display of start and end in point attribute. 
						Removes '.'  from end of namePart
						Removes '.'  from end of role
						880 - updated handling of 880 fields to improve mapping for related items.
						Replaced templates sufieldSelect SpecialSubfieldSelect and chopPunctuation with local functions.
						local:stripPunctation function to tests for initials and leaves punctuation if needed.						
		Revision 1.02 - Added Log Comment  2003/03/24 19:37:42  ckeith
	-->

	<!-- Local functions -->
	<!--  
		Strips punctuation from the last character of a field.
		To extend add any additional punctuation to the punctuation variable
		$str: string to be analyzed
	-->
	<xsl:function name="local:stripPunctuation">
		<xsl:param name="str"/>
		<xsl:variable name="punctuation">.:,;/ </xsl:variable>
		<!-- isolates end of string -->
		<xsl:variable name="strEnd">
			<xsl:value-of select="substring($str,(string-length($str)))"/>
		</xsl:variable>
		<!-- isolates last three characters in string tests for initials -->
		<xsl:variable name="initialTest">
			<xsl:variable name="lastChars" select="substring($str,(string-length($str)) - 2)"/>
			<xsl:choose>
				<xsl:when test="matches($lastChars,'^([.][A-Z][.])')">true</xsl:when>
				<xsl:when test="matches($lastChars,'^([ ][A-Z][.])')">true</xsl:when>
				<xsl:when test="matches($lastChars,'^([.][.][.])')">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($punctuation,$strEnd)">
				<xsl:choose>
					<!-- if ends with initials or ellipse then leave, otherwise remove ending punctuation -->
					<xsl:when test="$initialTest = 'true'">
						<xsl:value-of select="$str"/>
					</xsl:when>
					<!-- if ends with multiple punctiation -->
					<xsl:when
						test="matches(substring($str,(string-length($str))-3),'([.:,;/][ ][.:,;/])')">
						<xsl:value-of select="substring($str,1,string-length($str)-3)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="substring($str,1,string-length($str)-1)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$str"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- 
		Strips punctuation from the last character of a field, this version accepts two parameters:
		 $str: string to be striped
		 $pcodes: optional punctuation to override the defualt, if empty uses defualts
	 -->
	<xsl:function name="local:stripPunctuation">
		<xsl:param name="str"/>
		<xsl:param name="pcodes"/>
		<xsl:variable name="punctuation">
			<xsl:choose>
				<xsl:when test="$pcodes = ''">.:,;/ </xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$pcodes"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="strEnd">
			<xsl:value-of select="substring($str,(string-length($str)))"/>
		</xsl:variable>
		<xsl:variable name="initialTest">
			<xsl:variable name="lastChars" select="substring($str,(string-length($str)) - 1)"/>
			<xsl:choose>
				<xsl:when test="matches($lastChars,'^([.][A-Z][.])')">true</xsl:when>
				<xsl:when test="matches($lastChars,'^([ ][A-Z][.])')">true</xsl:when>
				<xsl:when test="matches($lastChars,'^([.][.][.])')">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="contains($punctuation,$strEnd)">
				<xsl:choose>
					<xsl:when test="$initialTest = 'true'">
						<xsl:value-of select="$str"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="substring($str,1,string-length($str)-1)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$str"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<!-- 
		Concats specified subfields in order they appear in marc record
		Two prameters
		$datafield: marc:datafield node to use for processing
		$codes: list of subfield codes, no spaces
	-->
	<xsl:function name="local:subfieldSelect">
		<xsl:param name="datafield" as="node()"/>
		<xsl:param name="codes"/>
		<!-- Breaks out codes for a for each loop -->
		<xsl:variable name="codeString">
			<xsl:for-each select="$codes">
				<xsl:apply-templates/>
			</xsl:for-each>
		</xsl:variable>
		<!-- Selects and prints out datafield -->
		<xsl:variable name="str">
			<xsl:for-each select="$datafield/child::*[contains($codes,@code)]">
				<xsl:value-of select="concat(.,' ')"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:value-of select="substring($str,1,string-length($str)-1)"/>
	</xsl:function>

	<!-- 
		Concats specified subfields in order they appear in marc record with specified delimiter 
		Three parameters
		$datafield: marc:datafield node to use for processing
		$codes: list of subfield codes, no spaces
		$delimiter: delimiter to use when concating subfields, if empty a space is used
	-->
	<xsl:function name="local:subfieldSelect">
		<xsl:param name="datafield" as="node()"/>
		<xsl:param name="codes"/>
		<xsl:param name="delimiter"/>
		<xsl:variable name="delimStr">
			<xsl:choose>
				<xsl:when test="$delimiter = ''">
					<xsl:value-of select="' '"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$delimiter"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- Breaks out codes for a for each loop -->
		<xsl:variable name="codeString">
			<xsl:for-each select="$codes">
				<xsl:apply-templates/>
			</xsl:for-each>
		</xsl:variable>
		<!-- Selects and prints out datafield -->
		<xsl:variable name="str">
			<xsl:for-each select="$datafield/child::*[contains($codes,@code)]">
				<xsl:value-of select="concat(.,$delimStr)"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:value-of select="substring($str,1,string-length($str)-1)"/>
	</xsl:function>

	<!-- 
		Concats specified subfields based on siblings
		$anyCode: the subfield you want to select on
		$axis: the that beforeCodes and afterCodes are computed
		$beforeCodes: subfields that occure before the axis
		$afterCodes: subfields that occure after the axis
	-->
	<xsl:function name="local:specialSubfieldSelect">
		<xsl:param name="datafield" as="node()"/>
		<xsl:param name="anyCodes"/>
		<xsl:param name="axis"/>
		<xsl:param name="beforeCodes"/>
		<xsl:param name="afterCodes"/>
		<xsl:variable name="codeString">
			<xsl:for-each select="$anyCodes">
				<xsl:apply-templates/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="subfieldStr">
			<xsl:for-each
				select="$datafield/child::*[contains($anyCodes, @code) 
				or (contains($beforeCodes,@code) and following-sibling::*[@code=$axis])  
				or (contains($afterCodes,@code) and preceding-sibling::*[@code=$axis])]">
				<xsl:apply-templates/>
				<xsl:text> </xsl:text>
			</xsl:for-each>
		</xsl:variable>
		<xsl:value-of select="substring($subfieldStr,1,string-length($subfieldStr)-1)"/>
	</xsl:function>

	<!-- Build MODS root element -->
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>

	<!-- Build marc:collection -->
	<xsl:template match="marc:collection">
		<modsCollection xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd">
			<xsl:apply-templates select="marc:record"/>
		</modsCollection>
	</xsl:template>
	<!-- Empty template for holdings -->
	<xsl:template match="marc:record[@type='Holdings']"/>
	<!-- Build marc:record -->
	<xsl:template match="marc:record">
		<!-- Leader and controlfield variables -->
		<xsl:variable name="leader" select="marc:leader"/>
		<xsl:variable name="leader6" select="substring($leader,7,1)"/>
		<xsl:variable name="leader7" select="substring($leader,8,1)"/>
		<xsl:variable name="leader19" select="substring($leader,20,1)"/>
		<xsl:variable name="controlField008" select="marc:controlfield[@tag='008']"/>
		<xsl:variable name="typeOf008">
			<xsl:choose>
				<xsl:when test="$leader6='a'">
					<xsl:choose>
						<xsl:when
							test="$leader7='a' or $leader7='c' or $leader7='d' or $leader7='m'"
							>BK</xsl:when>
						<xsl:when test="$leader7='b' or $leader7='i' or $leader7='s'">SE</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$leader6='t'">BK</xsl:when>
				<xsl:when test="$leader6='p'">MM</xsl:when>
				<xsl:when test="$leader6='m'">CF</xsl:when>
				<xsl:when test="$leader6='e' or $leader6='f'">MP</xsl:when>
				<xsl:when test="$leader6='g' or $leader6='k' or $leader6='o' or $leader6='r'"
					>VM</xsl:when>
				<xsl:when test="$leader6='c' or $leader6='d' or $leader6='i' or $leader6='j'"
					>MU</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<mods version="3.6">
			<!-- Adds appropriate namespaces and schema declaration if root is marc:record -->
			<xsl:if test="/marc:record">
				<xsl:namespace name="xsi">http://www.w3.org/2001/XMLSchema-instance</xsl:namespace>
				<xsl:attribute name="xsi:schemaLocation">http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd</xsl:attribute>
			</xsl:if>

			<!-- Call titleInfo templates and linking fields-->
			<xsl:apply-templates
				select="marc:datafield[@tag='245'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')]"
				mode="titleInfo"/>
			<!-- titleInfo type="abbreviated" -->
			<xsl:apply-templates
				select="marc:datafield[@tag='210'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'210')]"
				mode="titleInfo"/>
			<!-- titleInfo type="translated"-->
			<xsl:apply-templates
				select="marc:datafield[@tag='246'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')]"
				mode="titleInfo"/>
			<!-- titleInfo  type="uniform" -->
			<xsl:apply-templates
				select="marc:datafield[@tag='130'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'130')]"
				mode="titleInfo"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='240'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')]"
				mode="titleInfo"/>
			<!-- 2.22 removed [@ind2 !='2'] -->
			<xsl:apply-templates
				select="marc:datafield[@tag='740'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'740')]"
				mode="titleInfo"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='730'][@ind2 !='2'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')]"
				mode="titleInfo"/>
			<!-- titleInfo type="translated" -->
			<xsl:apply-templates select="marc:datafield[@tag='242']" mode="titleInfo"/>

			<!-- Call name templates -->
			<xsl:apply-templates
				select="marc:datafield[@tag='100'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'100')]"
				mode="name"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='110'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'110')]"
				mode="name"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='111'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'111')]"
				mode="name"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='700'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'700')][not(marc:subfield[@code='t'])]"
				mode="name"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='710'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'710')][not(marc:subfield[@code='t'])]"
				mode="name"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='711'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'711')][not(marc:subfield[@code='t'])]"
				mode="name"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='720'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'720')]"
				mode="name"/>

			<!-- Call typeOfResource -->
			<xsl:call-template name="typeOfResource">
				<xsl:with-param name="leader" select="$leader"/>
				<xsl:with-param name="leader6" select="$leader6"/>
				<xsl:with-param name="leader7" select="$leader7"/>
			</xsl:call-template>

			<!-- Call genre template -->
			<xsl:call-template name="genre">
				<!-- leader leader6 leader7 leader19 controlField008 typeOf008 -->
				<xsl:with-param name="controlField008" select="$controlField008"/>
				<xsl:with-param name="typeOf008" select="$typeOf008"/>
			</xsl:call-template>

			<!-- Call orginInfo template -->
			<xsl:call-template name="orginInfo">
				<xsl:with-param name="leader6" select="$leader6"/>
				<xsl:with-param name="leader7" select="$leader7"/>
				<xsl:with-param name="leader19" select="$leader19"/>
				<xsl:with-param name="typeOf008" select="$typeOf008"/>
				<xsl:with-param name="controlField008" select="$controlField008"/>
			</xsl:call-template>
			<!--			<xsl:apply-templates select="marc:datafield[@tag='880']"/>-->
			<xsl:apply-templates
				select="marc:datafield[@tag='264'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'264')]"
				mode="orginInfo"/>
			<!-- Call language templates -->
			<xsl:apply-templates select="marc:controlfield[@tag='008']" mode="lang"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='041'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'041')]"
				mode="lang"/>

			<!-- Call physicalDescription template -->
			<xsl:call-template name="physicalDescription">
				<xsl:with-param name="typeOf008" select="$typeOf008"/>
				<xsl:with-param name="controlField008" select="$controlField008"/>
				<xsl:with-param name="leader6" select="$leader6"/>
			</xsl:call-template>

			<!-- Call abstract template-->
			<xsl:apply-templates
				select="marc:datafield[@tag='520'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'520')]"
				mode="abstract"/>

			<!-- Call toc template-->
			<xsl:apply-templates
				select="marc:datafield[@tag='505'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'505')]"
				mode="toc"/>

			<!-- Call targetAudience templates-->
			<xsl:call-template name="targetAudience">
				<xsl:with-param name="typeOf008" select="$typeOf008"/>
				<xsl:with-param name="controlField008" select="$controlField008"/>
			</xsl:call-template>

			<!-- Call accessConditions templates -->
			<xsl:apply-templates
				select="marc:datafield[@tag='506'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'506')]"
				mode="accessConditions"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='540'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'540')]"
				mode="accessConditions"/>

			<!-- 
			Call note templates: 245c 362az 502-585 5XX
			NOTE: to order them call each explicitly
		-->
			<xsl:apply-templates
				select="marc:datafield[@tag='245'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')]
				| marc:datafield[@tag='362'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'362')]
				| marc:datafield[starts-with(@tag,'5')] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'5')]"
				mode="note"/>

			<!-- Call subject templates -->
			<!-- 034: cartographics -->
			<xsl:apply-templates
				select="marc:datafield[@tag='034'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'034')]"
				mode="subject"/>
			<!-- 255: cartographics -->
			<xsl:apply-templates
				select="marc:datafield[@tag='255'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'255')]"
				mode="subject"/>

			<!-- 043:  geographicCode -->
			<xsl:apply-templates
				select="marc:datafield[@tag='043'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'043')]"
				mode="subject"/>

			<!-- 045: temporal -->
			<xsl:apply-templates
				select="marc:datafield[@tag='045'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'045')]"
				mode="subject"/>

			<!-- 6XX: subjects -->
			<xsl:apply-templates
				select="marc:datafield[@tag='600'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'600')] 
				| marc:datafield[@tag='610'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'610')] 
				| marc:datafield[@tag='611'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'611')]
				| marc:datafield[@tag='630'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'630')]
				| marc:datafield[@tag='648'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'648')]
				| marc:datafield[@tag='650'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'650')]
				| marc:datafield[@tag='651'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'651')]
				| marc:datafield[@tag='653'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'653')]
				| marc:datafield[@tag='656'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'656')]
				| marc:datafield[@tag='662'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'662')]
				| marc:datafield[@tag='752'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'752')]"
				mode="subject"/>

			<!-- Call classification templates 0XX-->
			<xsl:apply-templates
				select="marc:datafield[@tag='050'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'050')]"
				mode="classification"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='060'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'060')]"
				mode="classification"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='080'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'080')]"
				mode="classification"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='082'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'082')]"
				mode="classification"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='084'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'084')]"
				mode="classification"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='086'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'086')]"
				mode="classification"/>

			<!-- Call location templates 852 and 856 -->
			<xsl:apply-templates
				select="marc:datafield[@tag='852'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'852')]"
				mode="location"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='856'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')]"
				mode="location"/>

			<!-- Call templates for related item -->
			<!-- series -->
			<xsl:apply-templates select="marc:datafield[@tag='490'][@ind1='0']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='440']" mode="relatedItem"/>
			<!-- isReferencedBy -->
			<xsl:apply-templates select="marc:datafield[@tag='510']" mode="relatedItem"/>
			<!-- original -->
			<xsl:apply-templates select="marc:datafield[@tag='534']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='700'][marc:subfield[@code='t']]"
				mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='710'][marc:subfield[@code='t']]"
				mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='711'][marc:subfield[@code='t']]"
				mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='730'][@ind2='2']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='740'][@ind2='2']" mode="relatedItem"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='760'] | marc:datafield[@tag='765'] | 
				marc:datafield[@tag='767'] | marc:datafield[@tag='762'] | 
				marc:datafield[@tag='770'] | marc:datafield[@tag='774'] | marc:datafield[@tag='775'] |
				marc:datafield[@tag='772'] | marc:datafield[@tag='773'] |
				marc:datafield[@tag='776'] | marc:datafield[@tag='780'] |
				marc:datafield[@tag='785'] | marc:datafield[@tag='786']"
				mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='800']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='810']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='811']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='830']" mode="relatedItem"/>
			<xsl:apply-templates select="marc:datafield[@tag='856'][@ind2='2']" mode="relatedItem"/>

			<!--  Call templates for identifiers -->
			<xsl:apply-templates
				select="marc:datafield[@tag='020'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'020')]"
				mode="id"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='024'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'024')]"
				mode="id"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='022'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'0222')]"
				mode="id"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='010'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'010')]"
				mode="id"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='028'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'0282')]"
				mode="id"/>
			<xsl:apply-templates select="marc:datafield[@tag='035']" mode="id"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='037'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'037')]"
				mode="id"/>
			<xsl:apply-templates
				select="marc:datafield[@tag='856']/marc:subfield[@code='u'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'8856')]/marc:subfield[@code='u']"
				mode="id"/>

			<!-- Create recordInfo template -->
			<recordInfo>
				<xsl:for-each select="marc:leader[substring($leader,19,1)='a']">
					<descriptionStandard>aacr</descriptionStandard>
				</xsl:for-each>

				<xsl:for-each select="marc:datafield[@tag='040']">
					<xsl:if test="marc:subfield[@code='e']">
						<descriptionStandard>
							<xsl:value-of select="marc:subfield[@code='e']"/>
						</descriptionStandard>
					</xsl:if>
					<recordContentSource authority="marcorg">
						<xsl:value-of select="marc:subfield[@code='a']"/>
					</recordContentSource>
				</xsl:for-each>
				<xsl:for-each select="marc:controlfield[@tag='008']">
					<recordCreationDate encoding="marc">
						<xsl:value-of select="substring(.,1,6)"/>
					</recordCreationDate>
				</xsl:for-each>

				<xsl:for-each select="marc:controlfield[@tag='005']">
					<recordChangeDate encoding="iso8601">
						<xsl:apply-templates/>
					</recordChangeDate>
				</xsl:for-each>
				<xsl:for-each select="marc:controlfield[@tag='001']">
					<recordIdentifier>
						<xsl:if test="../marc:controlfield[@tag='003']">
							<xsl:attribute name="source">
								<xsl:value-of select="../marc:controlfield[@tag='003']"/>
							</xsl:attribute>
						</xsl:if>
						<xsl:apply-templates/>
					</recordIdentifier>
				</xsl:for-each>

				<recordOrigin>Converted from MARCXML to MODS version 3.6 using
					MARC21slim2MODS3-6_XSL2-0.xsl (Revision 2.31 2016/3/15)</recordOrigin>

				<xsl:for-each select="marc:datafield[@tag='040']/marc:subfield[@code='b']">
					<languageOfCataloging>
						<languageTerm authority="iso639-2b" type="code">
							<xsl:apply-templates/>
						</languageTerm>
					</languageOfCataloging>
				</xsl:for-each>
			</recordInfo>
		</mods>
	</xsl:template>

	<!-- titleInfo Templates: 130 210 240 245 246 730 740 -->
	<!-- 130, 730 titleInfo uniform-->
	<xsl:template
		match="marc:datafield[@tag='130'] | marc:datafield[@tag='880'][@ind2!='2'][starts-with(marc:subfield[@code='6'],'130')]
		| marc:datafield[@tag='730'][@ind2!='2'] | marc:datafield[@tag='880'][@ind2!='2'][starts-with(marc:subfield[@code='6'],'730')]"
		mode="titleInfo">
		<titleInfo type="uniform">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<title>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'adfklmors'))"/>
			</title>
			<!-- part number and part name -->
			<xsl:apply-templates select="marc:subfield[@code='n'][1]" mode="titleInfo"/>
			<xsl:apply-templates select="marc:subfield[@code='p'][1]" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 210 titleInfo type abbreviated-->
	<xsl:template
		match="marc:datafield[@tag='210'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'210')]"
		mode="titleInfo">
		<titleInfo type="abbreviated">
			<!-- Add authority from subfield 2-->
			<xsl:if test="marc:subfield[@code='2']">
				<xsl:attribute name="authority">
					<xsl:value-of select="marc:subfield[@code='2']"/>
				</xsl:attribute>
			</xsl:if>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<title>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'a'))"/>
			</title>
			<!-- subtitle -->
			<xsl:apply-templates select="marc:subfield[@code='b']" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 242 titleInfo translated -->
	<xsl:template match="marc:datafield[@tag='242']" mode="titleInfo">
		<titleInfo type="translated">
			<!-- Added subfield $y-->
			<xsl:for-each select="marc:subfield[@code='y']">
				<xsl:attribute name="lang">
					<xsl:value-of select="text()"/>
				</xsl:attribute>
			</xsl:for-each>
			<xsl:variable name="title">
				<xsl:value-of select="local:subfieldSelect(.,'a')"/>
			</xsl:variable>
			<!-- Estabilish nonSort element when second indicator is not equal to 0 -->
			<xsl:choose>
				<!-- Adds nonSort element to title when ind2 is not 0 -->
				<xsl:when test="@ind2 != '0'">
					<!-- 2.30 -->
					<nonSort xml:space="preserve"><xsl:value-of
							select="substring(local:stripPunctuation($title),1,(@ind2 cast as xs:integer))"
						/> </nonSort>
					<title>
						<xsl:value-of
							select="substring(local:stripPunctuation($title),(@ind2 cast as xs:integer)+1)"
						/>
					</title>
				</xsl:when>
				<xsl:otherwise>
					<title>
						<xsl:value-of select="local:stripPunctuation($title)"/>
					</title>
				</xsl:otherwise>
			</xsl:choose>
			<!-- Subtitle -->
			<xsl:apply-templates select="marc:subfield[@code='b']" mode="titleInfo"/>
			<!-- Part number -->
			<xsl:apply-templates select="marc:subfield[@code='n'][1]" mode="titleInfo"/>
			<!-- Part name -->
			<xsl:apply-templates select="marc:subfield[@code='p'][1]" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 245 titleInfo main entry -->
	<xsl:template
		match="marc:datafield[@tag='245'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')]"
		mode="titleInfo">
		<titleInfo>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Selects subfields for title based on existance of subfield $b for subtitle -->
			<xsl:variable name="title">
				<xsl:choose>
					<xsl:when test="marc:subfield[@code='b']">
						<!-- ws 2.01 Added subfield s to title field -->
						<xsl:value-of select="local:specialSubfieldSelect(.,'','b','afgks','')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="local:subfieldSelect(.,'afgks')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!-- Estabilish nonSort element when second indicator is not equal to 0 -->
			<xsl:choose>
				<xsl:when test="@ind2 != '0'">
					<xsl:if test="@tag!='880'">
						<!-- 2.247-->
						<!--4/23/12 WS Added cast as xs:integer to treat ind2 values as integer not string -->
						<nonSort xml:space="preserve"><xsl:value-of
								select="substring(local:stripPunctuation($title),1,(@ind2 cast as xs:integer))"/> </nonSort>
					</xsl:if>
					<title>
						<!--4/23/12 WS Added cast as xs:integer to treat ind2 values as integer not string -->
						<xsl:value-of
							select="substring(local:stripPunctuation($title),(@ind2 cast as xs:integer)+1)"
						/>
					</title>
				</xsl:when>
				<xsl:otherwise>
					<title>
						<xsl:value-of select="local:stripPunctuation($title)"/>
					</title>
				</xsl:otherwise>
			</xsl:choose>
			<!-- Subtitle -->
			<xsl:apply-templates select="marc:subfield[@code='b']" mode="titleInfo"/>
			<!-- Part number -->
			<xsl:apply-templates select="marc:subfield[@code='n']" mode="titleInfo"/>
			<!-- Part name -->
			<xsl:apply-templates select="marc:subfield[@code='p'][1]" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 246 titleInfo type alternative -->
	<xsl:template
		match="marc:datafield[@tag='246'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')]"
		mode="titleInfo">
		<titleInfo>
			<!-- Select title type based on ind2 -->
			<xsl:choose>
				<xsl:when test="@ind2='1'">
					<xsl:attribute name="type">translated</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="type">alternative</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<!-- Add display label if subfield $i -->
			<xsl:if test="marc:subfield[@code='i']">
				<xsl:attribute name="displayLabel">
					<xsl:value-of select="local:subfieldSelect(.,'i')"/>
				</xsl:attribute>
			</xsl:if>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Create title -->
			<title>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'af'))"/>
			</title>
			<!-- Subtitle -->
			<xsl:apply-templates select="marc:subfield[@code='b']" mode="titleInfo"/>
			<!-- Part number -->
			<xsl:apply-templates select="marc:subfield[@code='n']" mode="titleInfo"/>
			<!-- Part name -->
			<xsl:apply-templates select="marc:subfield[@code='p'][1]" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 240 nameTitleGroup-->
	<xsl:template
		match="marc:datafield[@tag='240'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')]"
		mode="titleInfo">
		<titleInfo type="uniform">
			<!-- Add nameTitleGroup attribute if necessary -->
			<xsl:call-template name="nameTitleGroup"/>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:variable name="str1">
				<xsl:for-each select="marc:subfield">
					<xsl:if
						test="(contains('adfklmors',@code) and (not(../marc:subfield[@code='n' or @code='p']) or (following-sibling::marc:subfield[@code='n' or @code='p'])))">
						<xsl:value-of select="text()"/>
						<xsl:text> </xsl:text>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:variable name="str">
				<xsl:choose>
					<xsl:when test="ends-with($str1,' ')">
						<xsl:value-of select="substring($str1,1,string-length($str1)-1)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$str1"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!-- Create title -->
			<title>
				<xsl:value-of select="local:stripPunctuation($str)"/>
				<!--				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'adfklmors'))"/>-->
			</title>
			<!-- Subtitle -->
			<xsl:apply-templates select="marc:subfield[@code='b']" mode="titleInfo"/>
			<!-- Part number -->
			<xsl:apply-templates select="marc:subfield[@code='n']" mode="titleInfo"/>
			<!-- Part name -->
			<xsl:apply-templates select="marc:subfield[@code='p'][1]" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 740 titleInfo alternative -->
	<xsl:template
		match="marc:datafield[@tag='740'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'740')]"
		mode="titleInfo">
		<titleInfo type="alternative">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Title -->
			<title>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'ah'))"/>
			</title>
			<!-- Subtitle -->
			<xsl:apply-templates select="marc:subfield[@code='b']" mode="titleInfo"/>
			<!-- Part number -->
			<xsl:apply-templates select="marc:subfield[@code='n']" mode="titleInfo"/>
			<!-- Part name -->
			<xsl:apply-templates select="marc:subfield[@code='p'][1]" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- 130 730 245 246 240 740 210 titleInfo: common subfields -->
	<!-- $b titleInfo, subtitle -->
	<xsl:template match="marc:subfield[@code='b']" mode="titleInfo">
		<subTitle>
			<!-- NOTE: uses specialSubfieldSelect, which I don't know that we need -->
			<xsl:value-of
				select="local:stripPunctuation(local:specialSubfieldSelect(parent::*,'b','b','','afgk'))"
			/>
		</subTitle>
	</xsl:template>
	<!-- $n titleInfo, partNumber -->
	<xsl:template match="marc:subfield[@code='n']" mode="titleInfo">
		<xsl:variable name="partNumber">
			<xsl:choose>
				<xsl:when
					test="parent::*[@tag='245'] or parent::*[@tag='240'] or parent::*[@tag='130'] or parent::*[@tag='730']">
					<xsl:value-of
						select="local:specialSubfieldSelect(parent::*,'n','n','','fgkdlmor')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:if test="string-length($partNumber) &gt; 0">
			<partNumber>
				<xsl:value-of select="local:stripPunctuation($partNumber)"/>
			</partNumber>
		</xsl:if>
	</xsl:template>
	<!-- $p titleInfo, partName -->
	<xsl:template match="marc:subfield[@code='p']" mode="titleInfo">
		<xsl:variable name="partName">
			<xsl:choose>
				<xsl:when
					test="parent::*[@tag='245'] or parent::*[@tag='240'] or parent::*[@tag='130'] or parent::*[@tag='730']">
					<xsl:value-of
						select="local:specialSubfieldSelect(parent::*,'p','p','','fgkdlmor')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:if test="string-length($partName) &gt; 0">
			<partName>
				<xsl:value-of select="local:stripPunctuation($partName)"/>
			</partName>
		</xsl:if>
	</xsl:template>

	<!-- name templates -->
	<!-- 100 700 name-->
	<!-- 2.11 ws: added test to only output name element if no subfield t exists. -->
	<xsl:template
		match="marc:datafield[@tag='100'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'100')][not(marc:subfield[@code='t'])]
		| marc:datafield[@tag='700'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'700')][not(marc:subfield[@code='t'])]"
		mode="name">
		<name>
			<!-- name attributes -->
			<xsl:choose>
				<xsl:when test="@ind1='0' or @ind1='1'">
					<xsl:attribute name="type">personal</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='3'">
					<xsl:attribute name="type">family</xsl:attribute>
				</xsl:when>
				<xsl:otherwise/>
			</xsl:choose>
			<!-- add usage attribute if datafield 100 -->
			<xsl:if test="@tag='100'">
				<xsl:attribute name="usage">primary</xsl:attribute>
			</xsl:if>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Add nameTitleGroup attribute if necessary -->
			<xsl:call-template name="nameTitleGroup"/>
			<!-- namePart -->
			<xsl:for-each select="marc:subfield[@code='a']">
				<namePart>
					<xsl:value-of
						select="local:stripPunctuation(local:subfieldSelect(parent::*,'aq'))"/>
				</namePart>
			</xsl:for-each>
			<xsl:for-each select="marc:subfield[@code='b'] | marc:subfield[@code='c']">
				<namePart type="termsOfAddress">
					<xsl:value-of select="local:stripPunctuation(.)"/>
				</namePart>
			</xsl:for-each>
			<xsl:for-each select="marc:subfield[@code='d']">
				<namePart type="date">
					<xsl:value-of select="local:stripPunctuation(.)"/>
				</namePart>
			</xsl:for-each>
			<!-- affilitation -->
			<xsl:apply-templates select="marc:subfield[@code='u']" mode="affiliation"/>
			<!-- role -->
			<xsl:apply-templates select="marc:subfield[@code='e'] | marc:subfield[@code='4']"
				mode="role"/>
			<!-- 2.31 -->
			<xsl:apply-templates select="marc:subfield[@code='0'] " mode="identifier"/>
		</name>
	</xsl:template>

	<!-- 110 710 name-->
	<xsl:template
		match="marc:datafield[@tag='110'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'110')]
		| marc:datafield[@tag='710'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'710')][not(marc:subfield[@code='t'])]"
		mode="name">
		<xsl:if test="marc:subfield[@code!='t']">
			<name type="corporate">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<!-- Add nameTitleGroup attribute if necessary -->
				<xsl:call-template name="nameTitleGroup"/>
				<!-- namePart -->
				<xsl:for-each select="marc:subfield[@code='a'] | marc:subfield[@code='b']">
					<namePart>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</namePart>
				</xsl:for-each>
				<xsl:if
					test="marc:subfield[@code='c'] or marc:subfield[@code='d'] or marc:subfield[@code='n']">
					<namePart>
						<xsl:value-of select="local:subfieldSelect(.,'cdn')"/>
					</namePart>
				</xsl:if>
				<!-- role -->
				<xsl:apply-templates select="marc:subfield[@code='e'] | marc:subfield[@code='4']"
					mode="role"/>
				<!-- 2.31 -->
				<xsl:apply-templates select="marc:subfield[@code='0'] " mode="identifier"/>
			</name>
		</xsl:if>
	</xsl:template>

	<!-- 111 711 name -->
	<xsl:template
		match="marc:datafield[@tag='111'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'111')]
		| marc:datafield[@tag='711'][not(marc:subfield[@code='t'])] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'711')][not(marc:subfield[@code='t'])]"
		mode="name">
		<xsl:if test="marc:subfield[@code!='t']">
			<name type="conference">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<!-- Add nameTitleGroup attribute if necessary -->
				<xsl:call-template name="nameTitleGroup"/>
				<!-- namePart -->
				<namePart>
					<xsl:value-of select="local:subfieldSelect(.,'acdenq')"/>
				</namePart>
				<!-- role -->
				<xsl:apply-templates select="marc:subfield[@code='e'] | marc:subfield[@code='4']"
					mode="role"/>
				<!-- 2.31 -->
				<xsl:apply-templates select="marc:subfield[@code='0'] " mode="identifier"/>
			</name>
		</xsl:if>
	</xsl:template>

	<!-- 720 name -->
	<xsl:template
		match="marc:datafield[@tag='720'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'720')]"
		mode="name">
		<xsl:if test="marc:subfield[@code!='t']">
			<name>
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:if test="@ind1='1'">
					<xsl:attribute name="type">
						<xsl:text>personal</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:for-each select="marc:subfield[@code='a']">
					<namePart>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</namePart>
				</xsl:for-each>
				<!-- role -->
				<xsl:apply-templates select="marc:subfield[@code='e'] | marc:subfield[@code='4']"
					mode="role"/>
			</name>
		</xsl:if>
	</xsl:template>

	<!-- NOTE: Name subfield templates can be found starting on line 2593 -->

	<!-- Type of resource -->
	<xsl:template name="typeOfResource">
		<!-- Parameters passed from record template -->
		<xsl:param name="leader"/>
		<xsl:param name="leader6"/>
		<xsl:param name="leader7"/>
		<!-- Build type of resource element based on leader values -->
		<typeOfResource>
			<!-- Add appropriate attributes -->
			<xsl:if test="$leader7='c'">
				<xsl:attribute name="collection">yes</xsl:attribute>
			</xsl:if>
			<xsl:if test="$leader6='d' or $leader6='f' or $leader6='p' or $leader6='t'">
				<xsl:attribute name="manuscript">yes</xsl:attribute>
			</xsl:if>
			<!-- add controled resource type generated from leader -->
			<xsl:choose>
				<xsl:when test="$leader6='a' or $leader6='t'">text</xsl:when>
				<xsl:when test="$leader6='e' or $leader6='f'">cartographic</xsl:when>
				<xsl:when test="$leader6='c' or $leader6='d'">notated music</xsl:when>
				<xsl:when test="$leader6='i'">sound recording-nonmusical</xsl:when>
				<xsl:when test="$leader6='j'">sound recording-musical</xsl:when>
				<xsl:when test="$leader6='k'">still image</xsl:when>
				<xsl:when test="$leader6='g'">moving image</xsl:when>
				<xsl:when test="$leader6='o'">kit</xsl:when>
				<xsl:when test="$leader6='r'">three dimensional object</xsl:when>
				<xsl:when test="$leader6='m'">software, multimedia</xsl:when>
				<xsl:when test="$leader6='p'">mixed material</xsl:when>
			</xsl:choose>
		</typeOfResource>
	</xsl:template>

	<!-- Genre -->
	<xsl:template name="genre">
		<!-- Parameters passed from record template -->
		<xsl:param name="controlField008"/>
		<xsl:param name="typeOf008"/>
		<!-- Generates appropriate genre designation based on controlfield 008 values -->
		<xsl:if test="substring($controlField008,26,1)='d'">
			<genre authority="marcgt">globe</genre>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='r']">
			<genre authority="marcgt">remote-sensing image</genre>
		</xsl:if>
		<xsl:if test="$typeOf008='MP'">
			<xsl:variable name="controlField008-25" select="substring($controlField008,26,1)"/>
			<xsl:choose>
				<xsl:when
					test="$controlField008-25='a' or $controlField008-25='b' or $controlField008-25='c' or marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='j']">
					<genre authority="marcgt">map</genre>
				</xsl:when>
				<xsl:when
					test="$controlField008-25='e' or marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='d']">
					<genre authority="marcgt">atlas</genre>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="$typeOf008='SE'">
			<xsl:variable name="controlField008-21" select="substring($controlField008,22,1)"/>
			<xsl:choose>
				<xsl:when test="$controlField008-21='d'">
					<genre authority="marcgt">database</genre>
				</xsl:when>
				<xsl:when test="$controlField008-21='l'">
					<genre authority="marcgt">loose-leaf</genre>
				</xsl:when>
				<xsl:when test="$controlField008-21='m'">
					<genre authority="marcgt">series</genre>
				</xsl:when>
				<xsl:when test="$controlField008-21='n'">
					<genre authority="marcgt">newspaper</genre>
				</xsl:when>
				<xsl:when test="$controlField008-21='p'">
					<genre authority="marcgt">periodical</genre>
				</xsl:when>
				<xsl:when test="$controlField008-21='w'">
					<genre authority="marcgt">web site</genre>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="$typeOf008='BK' or $typeOf008='SE'">
			<xsl:variable name="controlField008-24" select="substring($controlField008,25,4)"/>
			<xsl:choose>
				<xsl:when test="contains($controlField008-24,'a')">
					<genre authority="marcgt">abstract or summary</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'b')">
					<genre authority="marcgt">bibliography</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'c')">
					<genre authority="marcgt">catalog</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'d')">
					<genre authority="marcgt">dictionary</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'e')">
					<genre authority="marcgt">encyclopedia</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'f')">
					<genre authority="marcgt">handbook</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'g')">
					<genre authority="marcgt">legal article</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'i')">
					<genre authority="marcgt">index</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'k')">
					<genre authority="marcgt">discography</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'l')">
					<genre authority="marcgt">legislation</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'m')">
					<genre authority="marcgt">theses</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'n')">
					<genre authority="marcgt">survey of literature</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'o')">
					<genre authority="marcgt">review</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'p')">
					<genre authority="marcgt">programmed text</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'q')">
					<genre authority="marcgt">filmography</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'r')">
					<genre authority="marcgt">directory</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'s')">
					<genre authority="marcgt">statistics</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'t')">
					<genre authority="marcgt">technical report</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'v')">
					<genre authority="marcgt">legal case and case notes</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'w')">
					<genre authority="marcgt">law report or digest</genre>
				</xsl:when>
				<xsl:when test="contains($controlField008-24,'z')">
					<genre authority="marcgt">treaty</genre>
				</xsl:when>
			</xsl:choose>
			<xsl:variable name="controlField008-29" select="substring($controlField008,30,1)"/>
			<xsl:choose>
				<xsl:when test="$controlField008-29='1'">
					<genre authority="marcgt">conference publication</genre>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="$typeOf008='CF'">
			<xsl:variable name="controlField008-26" select="substring($controlField008,27,1)"/>
			<xsl:choose>
				<xsl:when test="$controlField008-26='a'">
					<genre authority="marcgt">numeric data</genre>
				</xsl:when>
				<xsl:when test="$controlField008-26='e'">
					<genre authority="marcgt">database</genre>
				</xsl:when>
				<xsl:when test="$controlField008-26='f'">
					<genre authority="marcgt">font</genre>
				</xsl:when>
				<xsl:when test="$controlField008-26='g'">
					<genre authority="marcgt">game</genre>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="$typeOf008='BK'">
			<xsl:if test="substring($controlField008,25,1)='j'">
				<genre authority="marcgt">patent</genre>
			</xsl:if>
			<xsl:if test="substring($controlField008,25,1)='2'">
				<genre authority="marcgt">offprint</genre>
			</xsl:if>
			<xsl:if test="substring($controlField008,31,1)='1'">
				<genre authority="marcgt">festschrift</genre>
			</xsl:if>
			<xsl:variable name="controlField008-34" select="substring($controlField008,35,1)"/>
			<xsl:if
				test="$controlField008-34='a' or $controlField008-34='b' or $controlField008-34='c' or $controlField008-34='d'">
				<genre authority="marcgt">biography</genre>
			</xsl:if>
			<xsl:variable name="controlField008-33" select="substring($controlField008,34,1)"/>
			<xsl:choose>
				<xsl:when test="$controlField008-33='e'">
					<genre authority="marcgt">essay</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='d'">
					<genre authority="marcgt">drama</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='c'">
					<genre authority="marcgt">comic strip</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='l'">
					<genre authority="marcgt">fiction</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='h'">
					<genre authority="marcgt">humor, satire</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='i'">
					<genre authority="marcgt">letter</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='f'">
					<genre authority="marcgt">novel</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='j'">
					<genre authority="marcgt">short story</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='s'">
					<genre authority="marcgt">speech</genre>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="$typeOf008='MU'">
			<xsl:variable name="controlField008-30-31" select="substring($controlField008,31,2)"/>
			<xsl:if test="contains($controlField008-30-31,'b')">
				<genre authority="marcgt">biography</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'c')">
				<genre authority="marcgt">conference publication</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'d')">
				<genre authority="marcgt">drama</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'e')">
				<genre authority="marcgt">essay</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'f')">
				<genre authority="marcgt">fiction</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'o')">
				<genre authority="marcgt">folktale</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'h')">
				<genre authority="marcgt">history</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'k')">
				<genre authority="marcgt">humor, satire</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'m')">
				<genre authority="marcgt">memoir</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'p')">
				<genre authority="marcgt">poetry</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'r')">
				<genre authority="marcgt">rehearsal</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'g')">
				<genre authority="marcgt">reporting</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'s')">
				<genre authority="marcgt">sound</genre>
			</xsl:if>
			<xsl:if test="contains($controlField008-30-31,'l')">
				<genre authority="marcgt">speech</genre>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$typeOf008='VM'">
			<xsl:variable name="controlField008-33" select="substring($controlField008,34,1)"/>
			<xsl:choose>
				<xsl:when test="$controlField008-33='a'">
					<genre authority="marcgt">art original</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='b'">
					<genre authority="marcgt">kit</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='c'">
					<genre authority="marcgt">art reproduction</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='d'">
					<genre authority="marcgt">diorama</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='f'">
					<genre authority="marcgt">filmstrip</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='g'">
					<genre authority="marcgt">legal article</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='i'">
					<genre authority="marcgt">picture</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='k'">
					<genre authority="marcgt">graphic</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='l'">
					<genre authority="marcgt">technical drawing</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='m'">
					<genre authority="marcgt">motion picture</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='n'">
					<genre authority="marcgt">chart</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='o'">
					<genre authority="marcgt">flash card</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='p'">
					<genre authority="marcgt">microscope slide</genre>
				</xsl:when>
				<xsl:when
					test="$controlField008-33='q' or marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='q']">
					<genre authority="marcgt">model</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='r'">
					<genre authority="marcgt">realia</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='s'">
					<genre authority="marcgt">slide</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='t'">
					<genre authority="marcgt">transparency</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='v'">
					<genre authority="marcgt">videorecording</genre>
				</xsl:when>
				<xsl:when test="$controlField008-33='w'">
					<genre authority="marcgt">toy</genre>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
		<!-- Call genre datafields  047 and 655-->
		<xsl:apply-templates
			select="marc:datafield[@tag='047'][@ind2='7' or @ind2=' '] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'047')]"
			mode="genre"/>
		<!-- 2.12 ws: added new genre template for 336 field -->
		<xsl:apply-templates
			select="marc:datafield[@tag='336'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'336')]"
			mode="genre"/>
		<xsl:apply-templates
			select="marc:datafield[@tag='655'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'655')]"
			mode="genre"/>
	</xsl:template>

	<!-- 047, 336, 655 Genre datafields -->
	<xsl:template
		match="marc:datafield[@tag='047'][@ind2='7' or @ind2=' '] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'047')]"
		mode="genre">
		<genre authority="marcgt">
			<!-- 2.26 -->
			<xsl:choose>
				<xsl:when test="@ind2 = ' '">
					<xsl:attribute name="authority"><xsl:text>marcmuscomp</xsl:text></xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind2 = '7'">
					<xsl:if test="marc:subfield[@code='2']">
						<xsl:attribute name="authority">
							<xsl:value-of select="marc:subfield[@code='2']"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:when>
			</xsl:choose>
			<!-- 2.12 ws: added type = 'musical composition' as seen in mapping -->
			<xsl:attribute name="type">
				<xsl:text>musical composition</xsl:text>
			</xsl:attribute>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="local:subfieldSelect(.,'abcdef','-')"/>
		</genre>
	</xsl:template>

	<!-- 2.12 ws: added new genre template for 336 field -->
	<xsl:template
		match="marc:datafield[@tag='336'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'336')]"
		mode="genre">
		<genre authority="marcgt">
			<!--2.24 -->
			<xsl:if test="marc:subfield[@code='2']">
				<xsl:attribute name="authority">
					<xsl:value-of select="marc:subfield[@code='2']"/>
				</xsl:attribute>
			</xsl:if>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="local:subfieldSelect(.,'a','-')"/>
		</genre>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='655'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'655')]"
		mode="genre">
		<genre authority="marcgt">
			<!-- 2.25 --> 
			<xsl:choose>
				<xsl:when test="marc:subfield[@code='2']">
					<xsl:attribute name="authority">
						<xsl:value-of select="marc:subfield[@code='2']"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind2 != ' '">
					<xsl:attribute name="authority">
						<xsl:value-of select="@ind2"/>
					</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="local:subfieldSelect(.,'abvxyz','-')"/>
		</genre>
	</xsl:template>

	<!-- OrginInfo -->
	<xsl:template name="orginInfo">
		<!-- Leader and control field parameters passed from record template -->
		<xsl:param name="leader6"/>
		<xsl:param name="leader7"/>
		<xsl:param name="leader19"/>
		<xsl:param name="controlField008"/>
		<xsl:param name="typeOf008"/>
		<!-- Build main orginInfo element -->
		<xsl:if
			test="marc:datafield[@tag='044' or @tag='260' or @tag='046' or @tag='033' or @tag='250' or @tag='310' or @tag='321'] or marc:controlfield[@tag='008']">
			<originInfo>
				<xsl:call-template name="z2xx880"/>
				<!-- Variable for marc publication code, generated from controlfield 008 -->
				<xsl:variable name="MARCpublicationCode"
					select="normalize-space(substring($controlField008,16,3))"/>
				<!-- Build orginInfo element -->
				<!-- Build place elements -->
				<xsl:if test="translate($MARCpublicationCode,'|','')">
					<place>
						<placeTerm>
							<xsl:attribute name="type">code</xsl:attribute>
							<xsl:attribute name="authority">marccountry</xsl:attribute>
							<xsl:value-of select="$MARCpublicationCode"/>
						</placeTerm>
					</place>
				</xsl:if>
				<xsl:apply-templates select="marc:datafield[@tag='044']/marc:subfield[@code='c']"
					mode="orginInfo"/>
				<xsl:apply-templates select="marc:datafield[@tag='260']/child::*" mode="orginInfo">
					<xsl:with-param name="leader6" select="$leader6"/>
				</xsl:apply-templates>

				<!-- Build date elements -->
				<xsl:apply-templates select="marc:datafield[@tag='046']" mode="orginInfo"/>

				<!-- Variables for dates based on controlfield 008 and  -->
				<xsl:variable name="datafield260Str">
					<xsl:for-each select="marc:datafield[@tag='260']/marc:subfield[@code='c']">
						<xsl:value-of select="."/>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="dataField260c" select="local:stripPunctuation($datafield260Str)"/>
				<xsl:variable name="controlField008-7-10"
					select="normalize-space(substring($controlField008, 8, 4))"/>
				<xsl:variable name="controlField008-11-14"
					select="normalize-space(substring($controlField008, 12, 4))"/>
				<xsl:variable name="controlField008-6"
					select="normalize-space(substring($controlField008, 7, 1))"/>
				<!--Build date elements using controlfield 008 fields-->
				<xsl:if
					test="($controlField008-6='e' or $controlField008-6='p' or $controlField008-6='r' or $controlField008-6='s' or $controlField008-6='t') and ($leader6='d' or $leader6='f' or $leader6='p' or $leader6='t')">
					<xsl:if
						test="$controlField008-7-10 and ($controlField008-7-10 != $dataField260c)">
						<dateCreated encoding="marc">
							<xsl:value-of
								select="concat($controlField008-7-10, $controlField008-11-14)"/>
						</dateCreated>
					</xsl:if>
				</xsl:if>
				<xsl:if
					test="($controlField008-6='e' or $controlField008-6='p' or $controlField008-6='r' or $controlField008-6='s' or $controlField008-6='t') and not($leader6='d' or $leader6='f' or $leader6='p' or $leader6='t')">
					<xsl:if test="$controlField008-7-10 and ($controlField008-7-10 != $dataField260c)">
						<dateIssued encoding="marc">
							<xsl:value-of select="$controlField008-7-10"/>
						</dateIssued>
					</xsl:if>
				</xsl:if>
				<xsl:if
					test="$controlField008-6='c' or $controlField008-6='d' or $controlField008-6='i' or $controlField008-6='k' or $controlField008-6='m' or $controlField008-6='u'">
					<xsl:if test="$controlField008-7-10">
						<dateIssued encoding="marc" point="start">
							<xsl:value-of select="$controlField008-7-10"/>
						</dateIssued>
					</xsl:if>
				</xsl:if>
				<xsl:if
					test="$controlField008-6='c' or $controlField008-6='d' or $controlField008-6='i' or $controlField008-6='k' or $controlField008-6='m' or $controlField008-6='u'">
					<xsl:if test="$controlField008-11-14">
						<dateIssued encoding="marc" point="end">
							<xsl:value-of select="$controlField008-11-14"/>
						</dateIssued>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$controlField008-6='q'">
					<xsl:if test="$controlField008-7-10">
						<dateIssued encoding="marc" point="start" qualifier="questionable">
							<xsl:value-of select="$controlField008-7-10"/>
						</dateIssued>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$controlField008-6='q'">
					<xsl:if test="$controlField008-11-14">
						<dateIssued encoding="marc" point="end" qualifier="questionable">
							<xsl:value-of select="$controlField008-11-14"/>
						</dateIssued>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$controlField008-6='s'">
					<xsl:if test="$controlField008-7-10">
						<dateIssued encoding="marc">
							<xsl:value-of select="$controlField008-7-10"/>
						</dateIssued>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$controlField008-6='t'">
					<xsl:if test="$controlField008-11-14">
						<copyrightDate encoding="marc">
							<xsl:value-of select="$controlField008-11-14"/>
						</copyrightDate>
					</xsl:if>
				</xsl:if>

				<xsl:apply-templates select="marc:datafield[@tag='033']" mode="orginInfo">
					<xsl:with-param name="leader6" select="$leader6"/>
				</xsl:apply-templates>

				<!-- Build edition element -->
				<xsl:apply-templates select="marc:datafield[@tag='250']/marc:subfield[@code='a']"
					mode="orginInfo"/>

				<!-- Build issuance element -->
				<issuance>
					<xsl:choose>
						<xsl:when
							test="$leader7='a' or $leader7='c' or $leader7='d' or $leader7='m'"
							>monographic</xsl:when>
						<xsl:when test="$leader7='b'">continuing</xsl:when>
						<xsl:when
							test="$leader7='m' and ($leader19='a' or $leader19='b' or $leader19='c')"
							>multipart monograph</xsl:when>
						<!-- 2.23 20141218 -->
						<xsl:when test="$leader7='m' and ($leader19=' ')">single unit</xsl:when>
						<xsl:when test="$leader7='m' and ($leader19='#')">single unit</xsl:when>
						<xsl:when test="$leader7='i'">integrating resource</xsl:when>
						<xsl:when test="$leader7='s'">serial</xsl:when>
					</xsl:choose>
				</issuance>
				<!-- Build frequency element -->
				<xsl:apply-templates select="marc:datafield[@tag='310']|marc:datafield[@tag='321']"
					mode="orginInfo"/>
				<xsl:if test="$typeOf008='SE'">
					<xsl:variable name="controlField008-18"
						select="substring($controlField008,19,1)"/>
					<xsl:variable name="frequency">
						<frequency>
							<xsl:choose>
								<xsl:when test="$controlField008-18='a'">Annual</xsl:when>
								<xsl:when test="$controlField008-18='b'">Bimonthly</xsl:when>
								<xsl:when test="$controlField008-18='c'">Semiweekly</xsl:when>
								<xsl:when test="$controlField008-18='d'">Daily</xsl:when>
								<xsl:when test="$controlField008-18='e'">Biweekly</xsl:when>
								<xsl:when test="$controlField008-18='f'">Semiannual</xsl:when>
								<xsl:when test="$controlField008-18='g'">Biennial</xsl:when>
								<xsl:when test="$controlField008-18='h'">Triennial</xsl:when>
								<xsl:when test="$controlField008-18='i'">Three times a
									week</xsl:when>
								<xsl:when test="$controlField008-18='j'">Three times a
									month</xsl:when>
								<xsl:when test="$controlField008-18='k'">Continuously
									updated</xsl:when>
								<xsl:when test="$controlField008-18='m'">Monthly</xsl:when>
								<xsl:when test="$controlField008-18='q'">Quarterly</xsl:when>
								<xsl:when test="$controlField008-18='s'">Semimonthly</xsl:when>
								<xsl:when test="$controlField008-18='t'">Three times a
									year</xsl:when>
								<xsl:when test="$controlField008-18='u'">Unknown</xsl:when>
								<xsl:when test="$controlField008-18='w'">Weekly</xsl:when>
								<!-- 2.23 20141218 -->
								<xsl:when test="$controlField008-18=' '">Completely
									irregular</xsl:when>
								<xsl:when test="$controlField008-18='#'">Completely
									irregular</xsl:when>
								<xsl:otherwise/>
							</xsl:choose>
						</frequency>
					</xsl:variable>
					<xsl:if test="$frequency!=''">
						<frequency>
							<xsl:value-of select="$frequency"/>
						</frequency>
					</xsl:if>
				</xsl:if>
			</originInfo>
		</xsl:if>
		<!-- if linking fields add an additional orginInfo field -->
		<xsl:if
			test="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'260')] 
			| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'250')]/marc:subfield[@code='a']
			| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'044')]
			| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'046')]
			| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'033')]
			| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'310')]
			| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'321')]">
			<originInfo>
				<!-- Calls scriptCode template -->
				<xsl:for-each
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'260')] 
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'250')]
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'044')]
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'046')]
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'033')]
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'310')]
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'321')]">
					<xsl:choose>
						<xsl:when test="position()[1]">
							<xsl:call-template name="scriptCode"/>
						</xsl:when>
						<xsl:otherwise/>
					</xsl:choose>
				</xsl:for-each>
				<!-- Concats all altRepGroup numbers together -->
				<xsl:call-template name="altRepGroupOrginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'260')]/child::*"
					mode="orginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'250')]/marc:subfield[@code='a']"
					mode="orginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'044')]/marc:subfield[@code='c']"
					mode="orginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'046')]"
					mode="orginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'033')]"
					mode="orginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'310')]"
					mode="orginInfo"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'321')]"
					mode="orginInfo"/>
			</originInfo>
		</xsl:if>
	</xsl:template>
	<!-- 2.13 WS: added 264 to orginInfo -->
	<xsl:template match="marc:datafield[@tag='264']" mode="orginInfo">
		<xsl:if test="@ind2 ='0' or @ind2 ='1'or @ind2 ='2' or @ind2 ='3'">
			<originInfo>
				<!-- Removed displayLabel according to http://www.loc.gov/standards/mods/mods-mapping.html
				<xsl:choose>
					<xsl:when test="@ind2='0'">
						<xsl:attribute name="displayLabel">producer</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind2='1'">
						<xsl:attribute name="displayLabel">publisher</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind2='2'">
						<xsl:attribute name="displayLabel">distributor</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind2='3'">
						<xsl:attribute name="displayLabel">manufacturer</xsl:attribute>
					</xsl:when>
				</xsl:choose>
				-->
				<!-- 3.5 2.20 20142011 -->
				<xsl:choose>
					<xsl:when test="@ind2='0'">
						<xsl:attribute name="eventType">production</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind2='1'">
						<xsl:attribute name="eventType">publication</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind2='2'">
						<xsl:attribute name="eventType">distribution</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind2='3'">
						<xsl:attribute name="eventType">manufacture</xsl:attribute>
					</xsl:when>
				</xsl:choose>
				<xsl:call-template name="xxx880"/>
				<xsl:if test="marc:subfield[@code='a']">
					<place>
						<placeTerm type="text">
							<xsl:value-of select="local:subfieldSelect(.,'a')"/>
						</placeTerm>
					</place>
				</xsl:if>
				<xsl:if test="marc:subfield[@code='b']">
					<publisher>
						<xsl:value-of select="local:subfieldSelect(.,'b')"/>
					</publisher>
				</xsl:if>
				<xsl:if test="marc:subfield[@code='c']">
					<xsl:choose>
						<xsl:when test="@ind2='0'">
							<dateOther type="production">
								<xsl:value-of select="local:subfieldSelect(.,'c')"/>
							</dateOther>
						</xsl:when>
						<xsl:when test="@ind2='1'">
							<dateIssued>
								<xsl:value-of select="local:subfieldSelect(.,'c')"/>
							</dateIssued>
						</xsl:when>
						<xsl:when test="@ind2='2'">
							<dateOther type="distribution">
								<xsl:value-of select="local:subfieldSelect(.,'c')"/>
							</dateOther>
						</xsl:when>
						<xsl:when test="@ind2='3'">
							<dateOther type="manufacture">
								<xsl:value-of select="local:subfieldSelect(.,'c')"/>
							</dateOther>
						</xsl:when>
					</xsl:choose>
				</xsl:if>
			</originInfo>
		</xsl:if>
	</xsl:template>
	<!-- Concats all altRepGroup numbers together -->
	<xsl:template name="altRepGroupOrginInfo">
		<xsl:variable name="x260">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'260')]/marc:subfield[@code='6']">
				<xsl:variable name="sf06260" select="normalize-space(.)"/>
				<xsl:variable name="sf06260b" select="substring($sf06260, 5, 2)"/>
				<xsl:value-of select="$sf06260b"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x250">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'250')]/marc:subfield[@code='6']">
				<xsl:variable name="sf06250" select="normalize-space(.)"/>
				<xsl:variable name="sf06250b" select="substring($sf06250, 5, 2)"/>
				<xsl:value-of select="$sf06250b"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x044">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'044')]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x046">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'046')]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x033">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'033')]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x310">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'310')]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x321">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'321')]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="zx880" select="concat($x250, $x260, $x044, $x046, $x033, $x310, $x321)"/>
		<xsl:choose>
			<xsl:when test="$zx880 != ''">
				<xsl:attribute name="altRepGroup">
					<xsl:value-of select="$zx880"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>
	<!-- For OrginInfo linking fields -->
	<xsl:template name="z2xx880">
		<xsl:variable name="x260">
			<xsl:for-each select="marc:datafield[@tag='260']/marc:subfield[@code='6']">
				<xsl:variable name="sf06260" select="normalize-space(.)"/>
				<xsl:variable name="sf06260b" select="substring($sf06260, 5, 2)"/>
				<xsl:value-of select="$sf06260b"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x250">
			<xsl:for-each select="marc:datafield[@tag='250']/marc:subfield[@code='6']">
				<xsl:variable name="sf06250" select="normalize-space(.)"/>
				<xsl:variable name="sf06250b" select="substring($sf06250, 5, 2)"/>
				<xsl:value-of select="$sf06250b"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x044">
			<xsl:for-each select="marc:datafield[@tag='044']/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x046">
			<xsl:for-each select="marc:datafield[@tag='046']/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x033">
			<xsl:for-each select="marc:datafield[@tag='033']/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x310">
			<xsl:for-each select="marc:datafield[@tag='310']/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x321">
			<xsl:for-each select="marc:datafield[@tag='321']/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="zx880" select="concat($x250, $x260, $x044, $x046, $x033, $x310, $x321)"/>
		<xsl:choose>
			<xsl:when test="$zx880 != ''">
				<xsl:attribute name="altRepGroup">
					<xsl:value-of select="$zx880"/>
				</xsl:attribute>
				<xsl:call-template name="scriptCode"/>
			</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>

	<!-- orginInfo place 044 -->
	<xsl:template
		match="marc:datafield[@tag='044']/marc:subfield[@code='c'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'044')]/marc:subfield[@code='c']"
		mode="orginInfo">
		<place>
			<placeTerm>
				<xsl:attribute name="type">code</xsl:attribute>
				<xsl:attribute name="authority">iso3166</xsl:attribute>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</placeTerm>
		</place>
	</xsl:template>
	<!-- orginInfo place and date 260 -->
	<xsl:template
		match="marc:datafield[@tag='260']/child::* | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'260')]/child::*"
		mode="orginInfo">
		<xsl:param name="leader6"/>
		<xsl:choose>
			<xsl:when test="@code='a'">
				<place>
					<placeTerm>
						<xsl:attribute name="type">text</xsl:attribute>
						<xsl:variable name="str" select="."/>
						<xsl:choose>
							<xsl:when test="starts-with($str,'[')">
								<xsl:value-of
									select="local:stripPunctuation(local:stripPunctuation(substring(.,2)),']')"
								/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="local:stripPunctuation(.)"/>
							</xsl:otherwise>
						</xsl:choose>
					</placeTerm>
				</place>
			</xsl:when>
			<xsl:when test="@code='b'">
				<publisher>
					<xsl:value-of select="local:stripPunctuation(.,',:;/ ')"/>
				</publisher>
			</xsl:when>
			<xsl:when test="@code='c'">
				<xsl:choose>
					<xsl:when test="$leader6='d' or $leader6='f' or $leader6='p' or $leader6='t'">
						<dateCreated>
							<xsl:value-of select="local:stripPunctuation(.)"/>
						</dateCreated>
					</xsl:when>
					<xsl:otherwise>
						<dateIssued>
							<xsl:value-of select="local:stripPunctuation(.)"/>
						</dateIssued>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="@code='g'">
				<dateCreated>
					<xsl:apply-templates/>
				</dateCreated>
			</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>
	<!-- orginInfo date 046 033 -->
	<xsl:template
		match="marc:datafield[@tag='046'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'046')]"
		mode="orginInfo">
		<xsl:for-each select="marc:subfield[@code='m']">
			<dateValid point="start">
				<xsl:apply-templates/>
			</dateValid>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='n']">
			<dateValid point="end">
				<xsl:apply-templates/>
			</dateValid>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='j']">
			<dateModified>
				<xsl:apply-templates/>
			</dateModified>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='c']">
			<dateIssued encoding="marc" point="start">
				<xsl:apply-templates/>
			</dateIssued>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='e']">
			<dateIssued encoding="marc" point="end">
				<xsl:apply-templates/>
			</dateIssued>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='k']">
			<dateCreated encoding="marc" point="start">
				<xsl:apply-templates/>
			</dateCreated>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='l']">
			<dateCreated encoding="marc" point="end">
				<xsl:apply-templates/>
			</dateCreated>
		</xsl:for-each>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='033'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'033')]"
		mode="orginInfo">
		<xsl:for-each select=".[@ind1='0' or @ind1='1']/marc:subfield[@code='a']">
			<dateCaptured encoding="iso8601">
				<xsl:apply-templates/>
			</dateCaptured>
		</xsl:for-each>
		<xsl:for-each select=".[@ind1='2']/marc:subfield[@code='a'][1]">
			<dateCaptured encoding="iso8601" point="start">
				<xsl:apply-templates/>
			</dateCaptured>
		</xsl:for-each>
		<xsl:for-each select=".[@ind1='2']/marc:subfield[@code='a'][2]">
			<dateCaptured encoding="iso8601" point="end">
				<xsl:apply-templates/>
			</dateCaptured>
		</xsl:for-each>
	</xsl:template>
	<!-- orginInfo edition -->
	<xsl:template
		match="marc:datafield[@tag='250']/marc:subfield[@code='a'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'250')]/marc:subfield[@code='a']"
		mode="orginInfo">
		<edition>
			<xsl:apply-templates/>
		</edition>
	</xsl:template>
	<!-- orginInfo frequency -->
	<xsl:template
		match="marc:datafield[@tag='310']|marc:datafield[@tag='321'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'310')] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'321')]"
		mode="orginInfo">
		<frequency authority="marcfrequency">
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</frequency>
	</xsl:template>

	<!-- 008 041 Language elements -->
	<xsl:template match="marc:controlfield[@tag='008']" mode="lang">
		<!-- Isolates position 35-37 in controlfield 008 -->
		<xsl:variable name="controlField008-35-37"
			select="normalize-space(translate(substring(.,36,3),'|#',''))"/>
		<!-- Outputs language element based on value in position 35-37 -->
		<xsl:if test="$controlField008-35-37 != ''">
			<language>
				<languageTerm authority="iso639-2b" type="code">
					<xsl:value-of select="substring(.,36,3)"/>
				</languageTerm>
			</language>
		</xsl:if>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='041'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'041')]"
		mode="lang">
		<xsl:for-each
			select="marc:subfield[@code='a' or @code='b' or @code='d' or @code='e' or @code='f' or @code='g' or @code='h' or @code='j']">
			<!-- Creates language string for processing -->
			<xsl:variable name="langStr" select="."/>
			<!-- 
					Splits multiple language strings coded in a single marc:subfield 
					(based on string length, assumes each language is 3 characters long)
				-->
			<xsl:variable name="langStrMulti">
				<xsl:for-each-group select="string-to-codepoints($langStr)"
					group-by="(position() - 1) idiv 3">
					<temp>
						<xsl:sequence select="codepoints-to-string(current-group())"/>
					</temp>
				</xsl:for-each-group>
			</xsl:variable>
			<!-- Tests for multiple languages in single subfield, splits subfield into multiple language elements -->
			<xsl:choose>
				<xsl:when test="count($langStrMulti/child::*) &gt; 1">
					<xsl:for-each select="$langStrMulti/child::*">
						<language>
							<!-- Template checks for altRepGroup - 880 $6 -->
							<xsl:call-template name="xxx880"/>
							<languageTerm>
								<!-- Adds appropriate type and authority attributes -->
								<xsl:choose>
									<xsl:when test="../marc:subfield[@code='2']">
										<xsl:attribute name="authority">
											<xsl:value-of select="../marc:subfield[@code='2']"/>
										</xsl:attribute>
										<xsl:attribute name="type">code</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="authority">iso639-2b</xsl:attribute>
										<xsl:attribute name="type">code</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:apply-templates/>
							</languageTerm>
						</language>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<language>
						<!-- Addes objectPart attribute based on subcodes -->
						<xsl:choose>
							<xsl:when test="@code='b'">
								<xsl:attribute name="objectPart">summary</xsl:attribute>
							</xsl:when>
							<xsl:when test="@code='d'">
								<xsl:attribute name="objectPart">sung or spoken text</xsl:attribute>
							</xsl:when>
							<xsl:when test="@code='e'">
								<xsl:attribute name="objectPart">libretto</xsl:attribute>
							</xsl:when>
							<xsl:when test="@code='f'">
								<xsl:attribute name="objectPart">table of contents</xsl:attribute>
							</xsl:when>
							<xsl:when test="@code='g'">
								<xsl:attribute name="objectPart">accompanying
									material</xsl:attribute>
							</xsl:when>
							<xsl:when test="@code='h'">
								<xsl:attribute name="objectPart">translation</xsl:attribute>
							</xsl:when>
							<xsl:when test="@code='j'">
								<xsl:attribute name="objectPart">subtitle or caption</xsl:attribute>
							</xsl:when>
						</xsl:choose>
						<languageTerm>
							<!-- Adds appropriate type and authority attributes -->
							<xsl:choose>
								<xsl:when test="../marc:subfield[@code='2']">
									<xsl:attribute name="authority">
										<xsl:value-of select="../marc:subfield[@code='2']"/>
									</xsl:attribute>
									<xsl:attribute name="type">code</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="authority">iso639-2b</xsl:attribute>
									<xsl:attribute name="type">code</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:apply-templates/>
						</languageTerm>
					</language>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<!-- physicalDescription  -->
	<xsl:template name="physicalDescription">
		<xsl:param name="typeOf008"/>
		<xsl:param name="controlField008"/>
		<xsl:param name="leader6"/>
		<xsl:if
			test="marc:controlfield[@tag='007'] 
			or marc:datafield[@tag='130' or @tag='240' or @tag='242' or @tag='245' or @tag='246' or @tag='730']/child::*[@code='h'] 
			or marc:datafield[@tag='256']/child::*[@code='a'] 
			or marc:datafield[@tag='300']/child::*[@code='a' or @code='b' or @coe='c' or @code='e']
			or marc:datafield[@tag='351']/child::*[@code='3' or @code='a' or @coe='b' or @code='c']
			or marc:datafield[@tag='285']/child::*[@code='q'] 
			or marc:datafield[@tag='856']/child::*[@code='q']">
			<physicalDescription>
				<!--  880 field -->
				<xsl:call-template name="z3xx880"/>
				<xsl:call-template name="digitalOrigin">
					<xsl:with-param name="typeOf008" select="$typeOf008"/>
				</xsl:call-template>
				<xsl:call-template name="form">
					<xsl:with-param name="controlField008" select="$controlField008"/>
					<xsl:with-param name="typeOf008" select="$typeOf008"/>
					<xsl:with-param name="leader6" select="$leader6"/>
				</xsl:call-template>
				<xsl:call-template name="reformattingQuality"/>
				<xsl:apply-templates select="marc:datafield[@tag='856']/marc:subfield[@code='q']" mode="physDesc"/>
				<xsl:apply-templates select="marc:datafield[@tag='300']" mode="physDesc"/>
				<xsl:apply-templates select="marc:datafield[@tag='351']" mode="physDesc"/>
			</physicalDescription>
		</xsl:if>
		<xsl:if
			test="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'130')][child::*[@code='h']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')][child::*[@code='h']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'242')][child::*[@code='h']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')][child::*[@code='h']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')][child::*[@code='h']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')][child::*[@code='h']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'256')][child::*[@code='a']] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'300')] or 
			marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')][child::*[code='q']]">
			<physicalDescription>
				<!-- Calls scriptCode template -->
				<xsl:for-each
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'130')][child::*[@code='h']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')][child::*[@code='h']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'242')][child::*[@code='h']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')][child::*[@code='h']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')][child::*[@code='h']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')][child::*[@code='h']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'256')][child::*[@code='a']] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'300')] | 
					marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')][child::*[code='q']]">
					<xsl:choose>
						<xsl:when test="position()[1]">
							<xsl:call-template name="scriptCode"/>
						</xsl:when>
						<xsl:otherwise/>
					</xsl:choose>
				</xsl:for-each>
				<!-- Concats all altRepGroup numbers together -->
				<xsl:call-template name="altRepGroupPhysDsc"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'130')]/marc:subfield[@code='h'] 
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')]/marc:subfield[@code='h'] 
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'242')]/marc:subfield[@code='h'] 
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')]/marc:subfield[@code='h'] 
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')]/marc:subfield[@code='h'] 
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')]/marc:subfield[@code='h']
					| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'256')]/marc:subfield[@code='a']"
					mode="physDesc"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')]/child::*[code='q']"
					mode="physDesc"/>
				<xsl:apply-templates
					select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'300')]"
					mode="physDesc"/>
			</physicalDescription>
		</xsl:if>
	</xsl:template>

	<!-- Concats all altRepGroup numbers together -->
	<xsl:template name="altRepGroupPhysDsc">
		<xsl:variable name="x130">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'130')][child::*[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x240">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')][child::*[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x242">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'242')][child::*[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x245">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')][child::*[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x246">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')][child::*[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x730">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')][child::*[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x256">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'256')][child::*[@code='a']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x300">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'300')]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x856">
			<xsl:for-each
				select="marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')][child::*[@code='q']]//marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="zx880"
			select="concat($x130, $x240, $x242, $x245, $x246, $x730, $x256,$x300,$x856)"/>
		<xsl:choose>
			<xsl:when test="$zx880 != ''">
				<xsl:attribute name="altRepGroup">
					<xsl:value-of select="$zx880"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>
	<!-- For Physical Description linking fields -->
	<xsl:template name="z3xx880">
		<xsl:variable name="x130">
			<xsl:for-each
				select="marc:datafield[@tag='130'][marc:subfield[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x240">
			<xsl:for-each
				select="marc:datafield[@tag='240'][marc:subfield[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x242">
			<xsl:for-each
				select="marc:datafield[@tag='242'][marc:subfield[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x245">
			<xsl:for-each
				select="marc:datafield[@tag='245'][marc:subfield[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x246">
			<xsl:for-each
				select="marc:datafield[@tag='246'][marc:subfield[@code='h']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x730">
			<xsl:for-each
				select="marc:datafield[@tag='730']/marc:subfield[@code='6'][marc:subfield[@code='h']]">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x256">
			<xsl:for-each
				select="marc:datafield[@tag='256'][marc:subfield[@code='a']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x300">
			<xsl:for-each select="marc:datafield[@tag='300']/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="x856">
			<xsl:for-each
				select="marc:datafield[@tag='256'][marc:subfield[@code='q']]/marc:subfield[@code='6']">
				<xsl:variable name="altGrpStr" select="normalize-space(.)"/>
				<xsl:variable name="altGrp" select="substring($altGrpStr, 5, 2)"/>
				<xsl:value-of select="$altGrp"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="zx880"
			select="concat($x130, $x240, $x242, $x245, $x246, $x730, $x256,$x300,$x856)"/>
		<xsl:choose>
			<xsl:when test="$zx880 != ''">
				<xsl:attribute name="altRepGroup">
					<xsl:value-of select="$zx880"/>
				</xsl:attribute>
				<xsl:call-template name="scriptCode"/>
			</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>

	<!-- Templates used to build physicalDescription element -->
	<!-- 300 extent -->
	<xsl:template
		match="marc:datafield[@tag='300'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'300')]"
		mode="physDesc">
		<extent>
			<!-- 3.5 2.18 20142011 -->
			<xsl:if test="marc:subfield[@code='f']"><xsl:attribute name="unit"><xsl:value-of select="local:subfieldSelect(.,'f')"/></xsl:attribute></xsl:if>
			<xsl:value-of select="local:subfieldSelect(.,'abce3g')"/>
		</extent>
	</xsl:template>
	<!-- 351 note-->
	<xsl:template match="marc:datafield[@tag='351']" mode="physDesc">
		<note type="arrangement">
			<xsl:for-each select="marc:subfield[@code='3']">
				<xsl:apply-templates/>
				<xsl:text>: </xsl:text>
			</xsl:for-each>
			<xsl:value-of select="local:subfieldSelect(.,'abc')"/>
		</note>
	</xsl:template>
	<!-- 856 internetMediaType -->
	<xsl:template
		match="marc:datafield[@tag='856']/marc:subfield[@code='q'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')]/child::*[code='q']"
		mode="physDesc">
		<xsl:if test="string-length(.)&gt;1">
			<internetMediaType>
				<xsl:apply-templates/>
			</internetMediaType>
		</xsl:if>
	</xsl:template>

	<xsl:template name="reformattingQuality">
		<xsl:for-each select="marc:controlfield[@tag='007'][substring(text(),1,1)='c']">
			<xsl:choose>
				<xsl:when test="substring(text(),14,1)='a'">
					<reformattingQuality>access</reformattingQuality>
				</xsl:when>
				<xsl:when test="substring(text(),14,1)='p'">
					<reformattingQuality>preservation</reformattingQuality>
				</xsl:when>
				<xsl:when test="substring(text(),14,1)='r'">
					<reformattingQuality>replacement</reformattingQuality>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="digitalOrigin">
		<xsl:param name="typeOf008"/>
		<xsl:if test="$typeOf008='CF' and marc:controlfield[@tag='007'][substring(.,12,1)='a']">
			<digitalOrigin>reformatted digital</digitalOrigin>
		</xsl:if>
		<xsl:if test="$typeOf008='CF' and marc:controlfield[@tag='007'][substring(.,12,1)='b']">
			<digitalOrigin>digitized microfilm</digitalOrigin>
		</xsl:if>
		<xsl:if test="$typeOf008='CF' and marc:controlfield[@tag='007'][substring(.,12,1)='d']">
			<digitalOrigin>digitized other analog</digitalOrigin>
		</xsl:if>
	</xsl:template>
	<xsl:template name="form">
		<xsl:param name="controlField008"/>
		<xsl:param name="typeOf008"/>
		<xsl:param name="leader6"/>
		<!-- Variables used for caculating form element from controlfields -->
		<xsl:variable name="controlField008-23" select="substring($controlField008,24,1)"/>
		<xsl:variable name="controlField008-29" select="substring($controlField008,30,1)"/>
		<xsl:variable name="check008-23">
			<xsl:if test="$typeOf008='BK' or $typeOf008='MU' or $typeOf008='SE' or $typeOf008='MM'">
				<xsl:value-of select="true()"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="check008-29">
			<xsl:if test="$typeOf008='MP' or $typeOf008='VM'">
				<xsl:value-of select="true()"/>
			</xsl:if>
		</xsl:variable>
		<xsl:choose>
			<xsl:when
				test="($check008-23 and $controlField008-23='f') or ($check008-29 and $controlField008-29='f')">
				<form authority="marcform">braille</form>
			</xsl:when>
			<xsl:when
				test="($controlField008-23=' ' and ($leader6='c' or $leader6='d')) or (($typeOf008='BK' or $typeOf008='SE') and ($controlField008-23=' ' or $controlField008='r'))">
				<form authority="marcform">print</form>
			</xsl:when>
			<xsl:when
				test="$leader6 = 'm' or ($check008-23 and $controlField008-23='s') or ($check008-29 and $controlField008-29='s')">
				<form authority="marcform">electronic</form>
			</xsl:when>
			<xsl:when test="$leader6 = 'o'">
				<form authority="marcform">kit</form>
			</xsl:when>
			<xsl:when
				test="($check008-23 and $controlField008-23='b') or ($check008-29 and $controlField008-29='b')">
				<form authority="marcform">microfiche</form>
			</xsl:when>
			<xsl:when
				test="($check008-23 and $controlField008-23='a') or ($check008-29 and $controlField008-29='a')">
				<form authority="marcform">microfilm</form>
			</xsl:when>
		</xsl:choose>

		<!-- Form element generated from controlfield 007 -->
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='c']">
			<form authority="marccategory">electronic resource</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='b']">
			<form authority="marcsmd">chip cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='c']">
			<form authority="marcsmd">computer optical disc cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='j']">
			<form authority="marcsmd">magnetic disc</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='m']">
			<form authority="marcsmd">magneto-optical disc</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='o']">
			<form authority="marcsmd">optical disc</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='r']">
			<form authority="marcsmd">remote</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='a']">
			<form authority="marcsmd">tape cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='f']">
			<form authority="marcsmd">tape cassette</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='c'][substring(text(),2,1)='h']">
			<form authority="marcsmd">tape reel</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='d']">
			<form authority="marccategory">globe</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='d'][substring(text(),2,1)='a']">
			<form authority="marcsmd">celestial globe</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='d'][substring(text(),2,1)='e']">
			<form authority="marcsmd">earth moon globe</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='d'][substring(text(),2,1)='b']">
			<form authority="marcsmd">planetary or lunar globe</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='d'][substring(text(),2,1)='c']">
			<form authority="marcsmd">terrestrial globe</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='o']">
			<form authority="marccategory">kit</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='o'][substring(text(),2,1)='o']">
			<form authority="marcsmd">kit</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='a']">
			<form authority="marccategory">map</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='d']">
			<form authority="marcsmd">atlas</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='g']">
			<form authority="marcsmd">diagram</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='j']">
			<form authority="marcsmd">map</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='q']">
			<form authority="marcsmd">model</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='k']">
			<form authority="marcsmd">profile</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='r']">
			<form authority="marcsmd">remote-sensing image</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='s']">
			<form authority="marcsmd">section</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='a'][substring(text(),2,1)='y']">
			<form authority="marcsmd">view</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='h']">
			<form authority="marccategory">microform</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='a']">
			<form authority="marcsmd">aperture card</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='e']">
			<form authority="marcsmd">microfiche</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='f']">
			<form authority="marcsmd">microfiche cassette</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='b']">
			<form authority="marcsmd">microfilm cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='c']">
			<form authority="marcsmd">microfilm cassette</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='d']">
			<form authority="marcsmd">microfilm reel</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='h'][substring(text(),2,1)='g']">
			<form authority="marcsmd">microopaque</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='m']">
			<form authority="marccategory">motion picture</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='m'][substring(text(),2,1)='c']">
			<form authority="marcsmd">film cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='m'][substring(text(),2,1)='f']">
			<form authority="marcsmd">film cassette</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='m'][substring(text(),2,1)='r']">
			<form authority="marcsmd">film reel</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='k']">
			<form authority="marccategory">nonprojected graphic</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='n']">
			<form authority="marcsmd">chart</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='c']">
			<form authority="marcsmd">collage</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='d']">
			<form authority="marcsmd">drawing</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='o']">
			<form authority="marcsmd">flash card</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='e']">
			<form authority="marcsmd">painting</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='f']">
			<form authority="marcsmd">photomechanical print</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='g']">
			<form authority="marcsmd">photonegative</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='h']">
			<form authority="marcsmd">photoprint</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='i']">
			<form authority="marcsmd">picture</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='j']">
			<form authority="marcsmd">print</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='k'][substring(text(),2,1)='l']">
			<form authority="marcsmd">technical drawing</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='q']">
			<form authority="marccategory">notated music</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='q'][substring(text(),2,1)='q']">
			<form authority="marcsmd">notated music</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='g']">
			<form authority="marccategory">projected graphic</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='g'][substring(text(),2,1)='d']">
			<form authority="marcsmd">filmslip</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='g'][substring(text(),2,1)='c']">
			<form authority="marcsmd">filmstrip cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='g'][substring(text(),2,1)='o']">
			<form authority="marcsmd">filmstrip roll</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='g'][substring(text(),2,1)='f']">
			<form authority="marcsmd">other filmstrip type</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='g'][substring(text(),2,1)='s']">
			<form authority="marcsmd">slide</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='g'][substring(text(),2,1)='t']">
			<form authority="marcsmd">transparency</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='r']">
			<form authority="marccategory">remote-sensing image</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='r'][substring(text(),2,1)='r']">
			<form authority="marcsmd">remote-sensing image</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='s']">
			<form authority="marccategory">sound recording</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='e']">
			<form authority="marcsmd">cylinder</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='q']">
			<form authority="marcsmd">roll</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='g']">
			<form authority="marcsmd">sound cartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='s']">
			<form authority="marcsmd">sound cassette</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='d']">
			<form authority="marcsmd">sound disc</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='t']">
			<form authority="marcsmd">sound-tape reel</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='i']">
			<form authority="marcsmd">sound-track film</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='s'][substring(text(),2,1)='w']">
			<form authority="marcsmd">wire recording</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='f']">
			<form authority="marccategory">tactile material</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='f'][substring(text(),2,1)='c']">
			<form authority="marcsmd">braille</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='f'][substring(text(),2,1)='b']">
			<form authority="marcsmd">combination</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='f'][substring(text(),2,1)='a']">
			<form authority="marcsmd">moon</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='f'][substring(text(),2,1)='d']">
			<form authority="marcsmd">tactile, with no writing system</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='t']">
			<form authority="marccategory">text</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='t'][substring(text(),2,1)='c']">
			<form authority="marcsmd">braille</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='t'][substring(text(),2,1)='b']">
			<form authority="marcsmd">large print</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='t'][substring(text(),2,1)='a']">
			<form authority="marcsmd">regular print</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='t'][substring(text(),2,1)='d']">
			<form authority="marcsmd">text in looseleaf binder</form>
		</xsl:if>
		<xsl:if test="marc:controlfield[@tag='007'][substring(text(),1,1)='v']">
			<form authority="marccategory">videorecording</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='v'][substring(text(),2,1)='c']">
			<form authority="marcsmd">videocartridge</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='v'][substring(text(),2,1)='f']">
			<form authority="marcsmd">videocassette</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='v'][substring(text(),2,1)='d']">
			<form authority="marcsmd">videodisc</form>
		</xsl:if>
		<xsl:if
			test="marc:controlfield[@tag='007'][substring(text(),1,1)='v'][substring(text(),2,1)='r']">
			<form authority="marcsmd">videoreel</form>
		</xsl:if>

		<!-- Call form elements generated from datafields 130, 240, 242, 245, 246, 256 246, 730 -->
		<xsl:apply-templates
			select="marc:datafield[@tag='130']/marc:subfield[@code='h'] | marc:datafield[@tag='240']/marc:subfield[@code='h'] | 
			marc:datafield[@tag='242']/marc:subfield[@code='h'] | marc:datafield[@tag='245']/marc:subfield[@code='h'] 
			| marc:datafield[@tag='246']/marc:subfield[@code='h'] | marc:datafield[@tag='730']/marc:subfield[@code='h']
			| marc:datafield[@tag='256']/marc:subfield[@code='a'] | marc:datafield[@tag='337']/marc:subfield[@code='a'] | marc:datafield[@tag='338']/marc:subfield[@code='a']"
			mode="physDesc"/>
	</xsl:template>
	<!-- 130, 240, 242, 245, 246, 256 246, 730 form elements for physical description -->
	<!-- Form element generated from 130, 240, 242, 245, 246,730 and 256 datafields -->
	<!-- 
		NOTE: use replace to remove [ ] may be a problem if there are brackets that should be retained, 
		original script just removed first and last charachter using substring, but 1.0- does not have replace
		so that may have been why
	-->
	<xsl:template
		match="marc:datafield[@tag='130']/marc:subfield[@code='h'] 
		| marc:datafield[@tag='240']/marc:subfield[@code='h'] | marc:datafield[@tag='242']/marc:subfield[@code='h'] 
		| marc:datafield[@tag='245']/marc:subfield[@code='h'] | marc:datafield[@tag='246']/marc:subfield[@code='h'] 
		| marc:datafield[@tag='730']/marc:subfield[@code='h'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'130')]/marc:subfield[@code='h'] 
		| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')]/marc:subfield[@code='h'] 
		| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'242')]/marc:subfield[@code='h'] 
		| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')]/marc:subfield[@code='h'] 
		| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'246')]/marc:subfield[@code='h'] 
		| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')]/marc:subfield[@code='h']"
		mode="physDesc">
		<form authority="gmd">
			<xsl:variable name="str" select="local:stripPunctuation(.)"/>
			<xsl:value-of select="replace(replace($str,'\[',''),'\]','')"/>
		</form>
	</xsl:template>
	<!-- 2.12 ws: added field 337
		 2.15 tmee: added authority attribute for $2-->
	<xsl:template
		match="marc:datafield[@tag='337']/marc:subfield[@code='a'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'337')]/marc:subfield[@code='a']"
		mode="physDesc">
		<form>
			<xsl:attribute name="type">
				<xsl:text>media</xsl:text>
			</xsl:attribute>

			<xsl:attribute name="authority">
				<xsl:value-of select="../marc:subfield[@code='2']"/>
			</xsl:attribute>

			<xsl:apply-templates/>

		</form>
	</xsl:template>
	<!-- 2.12 ws: added field 338
		 2.15 tmee: added authority attribute for $2-->
	<xsl:template
		match="marc:datafield[@tag='338']/marc:subfield[@code='a'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'338')]/marc:subfield[@code='a']"
		mode="physDesc">
		<form>

			<xsl:attribute name="type">
				<xsl:text>carrier</xsl:text>
			</xsl:attribute>

			<xsl:attribute name="authority">
				<xsl:value-of select="../marc:subfield[@code='2']"/>
			</xsl:attribute>

			<xsl:apply-templates/>
		</form>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='256']/marc:subfield[@code='a'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'256')]/marc:subfield[@code='a']"
		mode="physDesc">
		<form>
			<xsl:apply-templates/>
		</form>
	</xsl:template>

	<!-- 520 Create abstract -->
	<xsl:template
		match="marc:datafield[@tag='520'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'520')]"
		mode="abstract">
		<abstract>
			<!-- Selects displayLabel attribute  -->
			<xsl:choose>
				<xsl:when test="@ind1='0'">
					<xsl:attribute name="displayLabel">Subject</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='1'">
					<xsl:attribute name="displayLabel">Review</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='2'">
					<xsl:attribute name="displayLabel">Scope and content</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='3'">
					<xsl:attribute name="displayLabel">Abstract</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='4'">
					<xsl:attribute name="displayLabel">Content advice</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='8'"/>
				<xsl:otherwise>
					<xsl:attribute name="displayLabel">Summary</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</abstract>
	</xsl:template>

	<!-- 505 Create table of contents element -->
	<xsl:template
		match="marc:datafield[@tag='505'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'505')]"
		mode="toc">
		<tableOfContents>
			<!-- Add displayLabel attribute -->
			<xsl:choose>
				<xsl:when test="@ind1='0'">
					<xsl:attribute name="displayLabel">Contents</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='1'">
					<xsl:attribute name="displayLabel">Incomplete contents</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='2'">
					<xsl:attribute name="displayLabel">Partial contents</xsl:attribute>
				</xsl:when>
				<xsl:otherwise/>
			</xsl:choose>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- add xlink:href attribute -->
			<xsl:call-template name="uri"/>
			<!-- Subfields  -->
			<xsl:value-of select="local:subfieldSelect(.,'agrt')"/>
		</tableOfContents>
	</xsl:template>

	<!-- 008 521 Create targetAudience -->
	<xsl:template name="targetAudience">
		<!-- Control field parameters passed from record template -->
		<xsl:param name="typeOf008"/>
		<xsl:param name="controlField008"/>
		<xsl:variable name="controlField008-22" select="substring($controlField008,23,1)"/>
		<!-- checks controlfield for appropriate values before generating element -->
		<xsl:if test="$typeOf008='BK' or 'CF' or 'MU' or 'VM'">
			<!-- Selects target audience based on 008/22 -->
			<xsl:choose>
				<xsl:when test="$controlField008-22='d'">
					<targetAudience authority="marctarget">adolescent</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='e'">
					<targetAudience authority="marctarget">adult</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='g'">
					<targetAudience authority="marctarget">general</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='b'">
					<targetAudience authority="marctarget">juvenile</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='c'">
					<targetAudience authority="marctarget">juvenile</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='j'">
					<targetAudience authority="marctarget">juvenile</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='a'">
					<targetAudience authority="marctarget">preschool</targetAudience>
				</xsl:when>
				<xsl:when test="$controlField008-22='f'">
					<targetAudience authority="marctarget">specialized</targetAudience>
				</xsl:when>
				<xsl:otherwise/>
			</xsl:choose>
		</xsl:if>
		<!--Adds adittional target audience for each 521 field -->
		<xsl:apply-templates
			select="marc:datafield[@tag='521'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'521')]"
			mode="targetAudience"/>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='521'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'521')]"
		mode="targetAudience">
		<targetAudience>
			<!-- Add displayLabel attribute -->
			<xsl:choose>
				<xsl:when test="@ind1='0'">
					<xsl:attribute name="displayLabel">Reading grade level</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='1'">
					<xsl:attribute name="displayLabel">Interest age level</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='2'">
					<xsl:attribute name="displayLabel">Interest grade level</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='3'">
					<xsl:attribute name="displayLabel">Special audience
						characteristics</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='4'">
					<xsl:attribute name="displayLabel">Motivation or interest level</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='8'"/>
				<xsl:otherwise>Audience</xsl:otherwise>
			</xsl:choose>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Values of subfields -->
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</targetAudience>
	</xsl:template>

	<!--506 540 Create accessCondition-->
	<xsl:template
		match="marc:datafield[@tag='506'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'506')]"
		mode="accessConditions">
		<accessCondition type="restrictionOnAccess">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Values of subfields -->
			<xsl:value-of select="local:subfieldSelect(.,'abcd35')"/>
		</accessCondition>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='540'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'540')]"
		mode="accessConditions">
		<accessCondition type="useAndReproduction">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Values of subfields -->
			<xsl:value-of select="local:subfieldSelect(.,'abcde35')"/>
		</accessCondition>
	</xsl:template>

	<!-- 245 362 5xx Create notes -->
	<xsl:template
		match="marc:datafield[@tag='245'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'245')]"
		mode="note">
		<xsl:if test="marc:subfield[@code='c']">
			<note type="statement of responsibility">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:if test="child::marc:subfield[@code='6']">
					<xsl:variable name="sf06"
						select="normalize-space(child::marc:subfield[@code='6'])"/>
					<xsl:variable name="sf06b" select="substring($sf06, 5, 2)"/>
					<xsl:variable name="scriptCode" select="substring($sf06, 8, 2)"/>
					<xsl:attribute name="altRepGroup">
						<!-- Concats the value of subfield 6 position 5 and 6 to create a unique value for note  -->
						<xsl:value-of select="$sf06b"/>
						<xsl:value-of select="$sf06b"/>
					</xsl:attribute>
					<xsl:call-template name="scriptCode"/>
				</xsl:if>
				<xsl:call-template name="scriptCode"/>
				<xsl:call-template name="uri"/>
				<xsl:value-of select="marc:subfield[@code='c']"/>
			</note>
		</xsl:if>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='362'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'362')]"
		mode="note">
		<note type="date/sequential designation">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'az')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='502'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'502')]"
		mode="note">
		<note type="thesis">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'abcdgo')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='504'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'504')]"
		mode="note">
		<note type="bibliography">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='508'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'508')]"
		mode="note">
		<note type="creation/production credits">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='511'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'511')]"
		mode="note">
		<note type="performers">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='515'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'515')]"
		mode="note">
		<note type="numbering">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='518'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'518')]"
		mode="note">
		<note type="venue">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='524'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'524')]"
		mode="note">
		<note type="preferred citation">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='530'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'530')]"
		mode="note">
		<note type="additional physical form">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'abcd')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='533'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'533')]"
		mode="note">
		<note type="reproduction">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'abcdefmn')"/>
		</note>
	</xsl:template>

	<!-- tmee 2.16
	<xsl:template match="marc:datafield[@tag='534'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'534')]" mode="note">
		<note type="original version">
			 Template checks for altRepGroup - 880 $6 
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'pabcefklmnt')"/>
		</note>
	</xsl:template>
	-->

	<xsl:template
		match="marc:datafield[@tag='535'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'535')]"
		mode="note">
		<note type="original location">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'abcd')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='536'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'536')]"
		mode="note">
		<note type="funding">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'abc')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='538'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'538')]"
		mode="note">
		<note type="system details">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'ai')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='541'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'541')]"
		mode="note">
		<note type="acquisition">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3noabcdefh')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='545'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'545')]"
		mode="note">
		<note type="biographical/historical">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='546'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'546')]"
		mode="note">
		<note type="language">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3ab')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='561'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'561')]"
		mode="note">
		<note type="ownership">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='562'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'562')]"
		mode="note">
		<note type="version identification">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3abcde')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='581'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'581')]"
		mode="note">
		<note type="publications">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3a')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='583'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'583')]"
		mode="note">
		<note type="action">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'abcdefhijklnoxz')"/>
		</note>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='585'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'585')]"
		mode="note">
		<note type="exhibitions">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:value-of select="local:subfieldSelect(.,'3a')"/>
		</note>
	</xsl:template>
	<!-- General Note field -->
	<xsl:template
		match="marc:datafield[@tag='500'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'500')]
		| marc:datafield[@tag='501'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'501')]
		| marc:datafield[@tag='507'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'507')]
		| marc:datafield[@tag='513'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'513')]
		| marc:datafield[@tag='514'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'514')]
		| marc:datafield[@tag='516'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'516')]
		| marc:datafield[@tag='522'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'522')]
		| marc:datafield[@tag='525'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'525')]
		| marc:datafield[@tag='526'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'526')]
		| marc:datafield[@tag='542'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'542')]
		| marc:datafield[@tag='544'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'544')]
		| marc:datafield[@tag='547'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'547')]
		| marc:datafield[@tag='550'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'550')]
		| marc:datafield[@tag='552'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'552')]
		| marc:datafield[@tag='555'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'555')]
		| marc:datafield[@tag='556'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'556')]
		| marc:datafield[@tag='563'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'563')]
		| marc:datafield[@tag='565'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'565')]
		| marc:datafield[@tag='567'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'567')]
		| marc:datafield[@tag='580'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'580')]
		| marc:datafield[@tag='584'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'584')]
		| marc:datafield[@tag='586'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'586')]
		| marc:datafield[@tag='588'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'588')]
		| marc:datafield[starts-with(@tag,'59')] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'59')]"
		mode="note">
		<note>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="uri"/>
			<xsl:for-each select="marc:subfield[not(@code='6' or @code='8')]">
				<xsl:apply-templates/>
				<xsl:if test="position() != last()">
					<xsl:text> </xsl:text>
				</xsl:if>
			</xsl:for-each>
		</note>
	</xsl:template>
	<!-- Suppresses any note fields not specifically matched above -->
	<xsl:template mode="note" match="marc:datafield"/>

	<!-- Create subjects -->
	<!-- 034 cartographics -->
	<xsl:template
		match="marc:datafield[@tag='034'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'034')]"
		mode="subject">
		<xsl:if test="marc:subfield[@code='d' or @code='e' or @code='f' or @code='g']">
			<subject>
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<cartographics>
					<coordinates>
						<xsl:value-of select="local:subfieldSelect(.,'defg')"/>
					</coordinates>
				</cartographics>
			</subject>
		</xsl:if>
	</xsl:template>

	<!-- 255 subject cartographics -->
	<xsl:template
		match="marc:datafield[@tag='255'][child::*[@code='a' or @code='b' or @code='c']]
		| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'255')][child::*[@code='a' or @code='b' or @code='c']]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:for-each select="marc:subfield[@code='a' or @code='b' or @code='c']">
				<cartographics>
					<xsl:if test="@code='a'">
						<scale>
							<xsl:apply-templates/>
						</scale>
					</xsl:if>
					<xsl:if test="@code='b'">
						<projection>
							<xsl:apply-templates/>
						</projection>
					</xsl:if>
					<xsl:if test="@code='c'">
						<coordinates>
							<xsl:apply-templates/>
						</coordinates>
					</xsl:if>
				</cartographics>
			</xsl:for-each>
		</subject>
	</xsl:template>

	<!-- 043 geographicCode -->
	<xsl:template
		match="marc:datafield[@tag='043']| marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'043')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:for-each select="marc:subfield[@code='a' or @code='b' or @code='c']">
				<geographicCode>
					<!-- Add authority atributes based on subcodes-->
					<xsl:attribute name="authority">
						<xsl:if test="@code='a'">
							<xsl:text>marcgac</xsl:text>
						</xsl:if>
						<xsl:if test="@code='b'">
							<xsl:value-of select="following-sibling::marc:subfield[@code=2]"/>
						</xsl:if>
						<xsl:if test="@code='c'">
							<xsl:text>iso3166</xsl:text>
						</xsl:if>
					</xsl:attribute>
					<xsl:apply-templates/>
				</geographicCode>
			</xsl:for-each>
		</subject>
	</xsl:template>

	<!-- 045 subject temporal -->
	<xsl:template
		match="marc:datafield[@tag='045'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'045')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:for-each select="marc:subfield[@code='b' or @code='c']">
				<temporal>
					<!-- Add appropriate encoding -->
					<xsl:if test="parent::*[@ind1='0' or @ind1='1' or @ind1='2']">
						<xsl:attribute name="encoding">iso8601</xsl:attribute>
					</xsl:if>
					<!-- Start and end points are added to subfield b if ind1='2' -->
					<xsl:if
						test="parent::*[@ind1='2'] and position() = 1 and count(../marc:subfield[@code='b']) &gt; 1">
						<xsl:attribute name="point">start</xsl:attribute>
					</xsl:if>
					<xsl:if
						test="parent::*[@ind1='2'] and position() = 2 and count(../marc:subfield[@code='b']) &gt; 1">
						<xsl:attribute name="point">end</xsl:attribute>
					</xsl:if>
					<!-- uses replace function to strip 'c' and 'd' from dates-->
					<xsl:value-of select="replace(replace(.,'d',''),'c','-')"/>
				</temporal>
			</xsl:for-each>
		</subject>
	</xsl:template>

	<!-- 6xx: Subjects -->
	<!-- 600 610 611 name -->
	<xsl:template
		match="marc:datafield[@tag='600'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'600')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<name type="personal">
				<!-- termsOfAddress subfields -->
				<xsl:apply-templates select="marc:subfield[@code='b' or @code='c']"
					mode="termsOfAddress"/>
				<!-- namePart subfields -->
				<xsl:apply-templates select="marc:subfield[@code='a']" mode="namePart"/>
				<!-- namePart date subfield -->
				<xsl:apply-templates select="marc:subfield[@code='d']" mode="nameDate"/>
				<!-- affiliation -->
				<xsl:apply-templates select="marc:subfield[@code='u']" mode="affiliation"/>
				<!-- role -->
				<xsl:apply-templates select="marc:subfield[@code='e'] | marc:subfield[@code='4']"
					mode="role"/>
			</name>
			<!-- title as subject -->
			<xsl:apply-templates select="marc:subfield[@code='t']" mode="subjectTitle"/>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<xsl:template
		match="marc:datafield[@tag='610'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'610')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<name type="corporate">
				<!-- namePart -->
				<xsl:apply-templates select="marc:subfield[@code='a']" mode="namePart"/>
				<xsl:apply-templates select="marc:subfield[@code='b']" mode="namePart"/>
				<!-- namePart -->
				<xsl:if test="marc:subfield[@code='c' or @code='d' or @code='n' or @code='p']">
					<namePart>
						<xsl:value-of select="local:subfieldSelect(.,'cdnp')"/>
					</namePart>
				</xsl:if>
				<!-- role -->
				<xsl:apply-templates select="marc:subfield[@code='e'] | marc:subfield[@code='4']"
					mode="role"/>
			</name>
			<!-- title as subject -->
			<xsl:apply-templates select="marc:subfield[@code='t']" mode="subjectTitle"/>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<xsl:template
		match="marc:datafield[@tag='611'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'611')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<name type="conference">
				<namePart>
					<xsl:value-of select="local:subfieldSelect(.,'abcdeqnp')"/>
				</namePart>
				<!-- role -->
				<xsl:apply-templates select="marc:subfield[@code='4']" mode="role"/>
			</name>
			<!-- title as subject -->
			<xsl:apply-templates select="marc:subfield[@code='t']" mode="subjectTitle"/>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<!-- 630: subject title -->
	<xsl:template
		match="marc:datafield[@tag='630'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'630')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<titleInfo>
				<title>
					<xsl:value-of
						select="local:stripPunctuation(local:subfieldSelect(.,'adfhklor'))"/>
				</title>
				<xsl:apply-templates select="marc:subfield[@code='n']" mode="titleInfo"/>
				<xsl:apply-templates select="marc:subfield[@code='p']" mode="titleInfo"/>
			</titleInfo>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<!-- 648: subject temporal -->
	<xsl:template
		match="marc:datafield[@tag='648'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'648')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<temporal>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'abcd'))"/>
			</temporal>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<!-- 650: subject topic -->
	<xsl:template
		match="marc:datafield[@tag='650'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'650')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<topic>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'abcd'))"/>
			</topic>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<!-- 651: subject geographic -->
	<xsl:template
		match="marc:datafield[@tag='651'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'651')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:call-template name="subjectAuthority"/>
			<xsl:call-template name="uri"/>
			<xsl:for-each select="marc:subfield[@code='a']">
				<geographic>
					<xsl:value-of select="local:stripPunctuation(.)"/>
				</geographic>
			</xsl:for-each>
			<!-- Additional subjects -->
			<xsl:apply-templates
				select="marc:subfield[@code='v' or @code='x' or @code='y' or @code='z']"
				mode="subject"/>
		</subject>
	</xsl:template>

	<!-- 653: subject, topics (Uncontrolled) -->
	<xsl:template
		match="marc:datafield[@tag='653'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'653')]"
		mode="subject">
		<xsl:if test="@ind2=' ' or @ind2='0'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<topic>
					<xsl:value-of select="."/>
				</topic>
				</xsl:for-each>
			</subject>
		</xsl:if>
		<xsl:if test="@ind2='1'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<name type="personal">
					<namePart>
						<xsl:value-of select="."/>
					</namePart>
				</name>
				</xsl:for-each>
			</subject>
		</xsl:if>
		<xsl:if test="@ind2='2'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<name type="corporate">
					<namePart>
						<xsl:value-of select="."/>
					</namePart>
				</name>
				</xsl:for-each>
			</subject>
		</xsl:if>
		<xsl:if test="@ind2='3'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<name type="conference">
					<namePart>
						<xsl:value-of select="."/>
					</namePart>
				</name>
				</xsl:for-each>
			</subject>
		</xsl:if>
		<xsl:if test="@ind2='4'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<temporal>
					<xsl:value-of select="."/>
				</temporal>
				</xsl:for-each>
			</subject>
		</xsl:if>
		<xsl:if test="@ind2='5'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<geographic>
					<xsl:value-of select="."/>
				</geographic>
				</xsl:for-each>
			</subject>
		</xsl:if>
		<xsl:if test="@ind2='6'">
			<subject>
				<xsl:for-each select="marc:subfield[@code='a']">
				<genre>
					<xsl:value-of select="."/>
				</genre>
				</xsl:for-each>
			</subject>
		</xsl:if>
	</xsl:template>

	<!-- 656: subject, occupation -->
	<xsl:template
		match="marc:datafield[@tag='656'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'656')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:if test="marc:subfield[@code='2']">
				<xsl:attribute name="authority">
					<xsl:value-of select="marc:subfield[@code='2']"/>
				</xsl:attribute>
			</xsl:if>
			<occupation>
				<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'a'))"/>
			</occupation>
		</subject>
	</xsl:template>

	<!-- 662 and 752; hierarchical geographic -->
	<xsl:template
		match="marc:datafield[@tag='662'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'662')] 
		| marc:datafield[@tag='752'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'752')]"
		mode="subject">
		<subject>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<hierarchicalGeographic>
				<!-- 2.31 -->
				<xsl:if test="marc:subfield[@code='0']">
					<xsl:attribute name="valueURI"><xsl:value-of select="marc:subfield[@code='0']"/></xsl:attribute>
				</xsl:if>
				<xsl:for-each select="marc:subfield[@code='a']">
					<country>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</country>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='b']">
					<state>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</state>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='c']">
					<county>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</county>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='d']">
					<city>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</city>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='e']">
					<citySection>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</citySection>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='g']">
					<area>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</area>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='h']">
					<extraterrestrialArea>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</extraterrestrialArea>
				</xsl:for-each>
			</hierarchicalGeographic>
		</subject>
	</xsl:template>

	<!-- 
		Subfield templates used by mods subject elements and 
		also by and names and titles as top level elements 
	-->
	<!-- subfield a and q: namePart -->
	<xsl:template match="marc:subfield[@code='a'] | marc:subfield[@code='b']" mode="namePart">
		<namePart>
			<xsl:choose>
				<xsl:when test="@code='a'">
					<xsl:value-of
						select="local:stripPunctuation(local:subfieldSelect(parent::*,'aq'))"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="local:stripPunctuation(.)"/>
				</xsl:otherwise>
			</xsl:choose>
		</namePart>
	</xsl:template>
	<!-- subfields b or c: termsOfAddress -->
	<xsl:template match="marc:subfield[@code='b' or @code='c']" mode="termsOfAddress">
		<namePart type="termsOfAddress">
			<xsl:value-of select="local:stripPunctuation(.,',:;/ ')"/>
		</namePart>
	</xsl:template>
	<!-- subfield b: name type='date' -->
	<xsl:template match="marc:subfield[@code='d']" mode="nameDate">
		<namePart type="date">
			<xsl:value-of select="local:stripPunctuation(.)"/>
		</namePart>
	</xsl:template>
	<!-- subfield u: affiliation -->
	<xsl:template match="marc:subfield[@code='u']" mode="affiliation">
		<affiliation>
			<xsl:apply-templates/>
		</affiliation>
	</xsl:template>
	<!-- subfields e and 4: role -->
	<xsl:template match="marc:subfield[@code='e' or @code='4']" mode="role">
		<role>
			<roleTerm>
				<xsl:choose>
					<xsl:when test="@code='e'">
						<xsl:attribute name="type">text</xsl:attribute>
					</xsl:when>
					<xsl:when test="@code='4'">
						<xsl:attribute name="authority">marcrelator</xsl:attribute>
						<xsl:attribute name="type">code</xsl:attribute>
					</xsl:when>
				</xsl:choose>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</roleTerm>
		</role>
	</xsl:template>
	
	<!-- 2.31 -->
	<xsl:template match="marc:subfield[@code='0']" mode="identifier">
		<nameIdentifier>
			<xsl:value-of select="."/>
		</nameIdentifier>
	</xsl:template>
	<!-- subfield v: genre -->
	<xsl:template match="marc:subfield[@code='v']" mode="subject">
		<genre>
			<xsl:value-of select="local:stripPunctuation(.)"/>
		</genre>
	</xsl:template>
	<!-- subfield x: topic -->
	<xsl:template match="marc:subfield[@code='x']" mode="subject">
		<topic>
			<xsl:value-of select="local:stripPunctuation(.)"/>
		</topic>
	</xsl:template>
	<!-- subfield y: temporal -->
	<xsl:template match="marc:subfield[@code='y']" mode="subject">
		<temporal>
			<xsl:value-of select="local:stripPunctuation(.)"/>
		</temporal>
	</xsl:template>
	<!-- subfield z: geographic -->
	<xsl:template match="marc:subfield[@code='z']" mode="subject">
		<geographic>
			<xsl:value-of select="local:stripPunctuation(.)"/>
		</geographic>
	</xsl:template>
	<!-- subfield t: titleInfo -->
	<xsl:template match="marc:subfield[@code='t']" mode="subjectTitle">
		<titleInfo>
			<title>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</title>
			<!-- Calls part number and name (uses subfield template used by titleInfo) -->
			<xsl:apply-templates select="../marc:subfield[@code='n']" mode="titleInfo"/>
			<xsl:apply-templates select="../marc:subfield[@code='p']" mode="titleInfo"/>
		</titleInfo>
	</xsl:template>

	<!-- Creates authority attribute for mods subject elements based on ind2 attribute.  -->
	<xsl:template name="subjectAuthority">
		<xsl:choose>
			<xsl:when test="@ind2='0'">
				<xsl:attribute name="authority">lcsh</xsl:attribute>
			</xsl:when>
			<xsl:when test="@ind2='1'">
				<xsl:attribute name="authority">lcshac</xsl:attribute>
			</xsl:when>
			<xsl:when test="@ind2='2'">
				<xsl:attribute name="authority">mesh</xsl:attribute>
			</xsl:when>
			<xsl:when test="@ind2='3'">
				<xsl:attribute name="authority">nal</xsl:attribute>
			</xsl:when>
			<xsl:when test="@ind2='5'">
				<xsl:attribute name="authority">csh</xsl:attribute>
			</xsl:when>
			<xsl:when test="@ind2='6'">
				<xsl:attribute name="authority">rvm</xsl:attribute>
			</xsl:when>
			<xsl:when test="@ind2='7'">
				<xsl:attribute name="authority">
					<xsl:value-of select="marc:subfield[@code='2']"/>
				</xsl:attribute>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- Classification templates -->
	<xsl:template
		match="marc:datafield[@tag='050'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'050')]"
		mode="classification">
		<xsl:for-each
			select="marc:subfield[@code='b'] | (marc:subfield[@code='a'][not(following-sibling::marc:subfield[@code='b'])])">
			<classification authority="lcc">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<!-- Add displayLabel -->
				<xsl:if test="../marc:subfield[@code='3']">
					<xsl:attribute name="displayLabel">
						<xsl:value-of select="../marc:subfield[@code='3']"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="not(self::*[@code='a'])">
					<xsl:value-of select="preceding-sibling::marc:subfield[@code='a'][1]"/>
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:apply-templates/>
			</classification>
		</xsl:for-each>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='060'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'060')]"
		mode="classification">
		<classification authority="nlm">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</classification>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='080'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'080')]"
		mode="classification">
		<classification authority="udc">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="local:subfieldSelect(.,'abx')"/>
		</classification>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='082'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'082')]"
		mode="classification">
		<classification authority="ddc">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:if test="marc:subfield[@code='2']">
				<xsl:attribute name="edition">
					<xsl:value-of select="marc:subfield[@code='2']"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</classification>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='084'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'084')]"
		mode="classification">
		<classification>
			<xsl:attribute name="authority">
				<xsl:value-of select="marc:subfield[@code='2']"/>
			</xsl:attribute>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</classification>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='086'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'086')]"
		mode="classification">
		<classification>
			<xsl:choose>
				<xsl:when test="@ind1='0'">
					<xsl:attribute name="authority">sudocs</xsl:attribute>
				</xsl:when>
				<xsl:when test="@ind1='1'">
					<xsl:attribute name="authority">candoc</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="marc:subfield[@code='2']">
						<xsl:attribute name="authority">
							<xsl:value-of select="marc:subfield[@code='2']"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:value-of select="marc:subfield[@code='a']"/>
		</classification>
	</xsl:template>

	<!-- Create location templates: 852 856 -->
	<xsl:template
		match="marc:datafield[@tag='852'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'852')]"
		mode="location">
		<location>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:if test="marc:subfield[@code='a' or @code='b' or @code='e' or  @code='u']">
				<physicalLocation>
					<xsl:if test="marc:subfield[@code='3']">
						<xsl:attribute name="displayLabel">
							<xsl:value-of select="marc:subfield[@code='3']"/>
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="marc:subfield[@code='u']">
						<xsl:call-template name="uri"/>
					</xsl:if>
					<xsl:value-of select="local:subfieldSelect(.,'abje')"/>
				</physicalLocation>
			</xsl:if>
			<xsl:if
				test="marc:subfield[@code='h' or @code='i' or @code='j' or @code='k' or @code='l' or @code='m' or @code='t']">
				<shelfLocator>
					<xsl:value-of select="local:subfieldSelect(.,'hijklmt')"/>
				</shelfLocator>
			</xsl:if>
			<!-- 2.29 -->
			<xsl:if test="marc:subfield[@code='p' or @code='t']">
				<holdingSimple>
					<copyInformation>
						<xsl:for-each select="marc:subfield[@code='p']|marc:subfield[@code='t']">
							<itemIdentifier>
								<xsl:if test="@code='t'">
									<xsl:attribute name="type"><xsl:text>copy number</xsl:text></xsl:attribute>
								</xsl:if>
								<xsl:apply-templates select="."/>
							</itemIdentifier>							
						</xsl:for-each>
					</copyInformation>
				</holdingSimple>
			</xsl:if>
		</location>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='856'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')]"
		mode="location">
		<xsl:if test="@ind2!='2' and marc:subfield[@code='u']">
			<location>
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<url displayLabel="electronic resource">
					<!-- 1.41 tmee AQ1.9 added choice protocol for @usage="primary display" -->
					<xsl:variable name="primary">
						<xsl:choose>
							<xsl:when
								test="@ind2='0' and count(preceding-sibling::marc:datafield[@tag='856'] [@ind2='0'])=0"
								>true</xsl:when>
							<xsl:when
								test="@ind2='1' and count(ancestor::marc:record//marc:datafield[@tag='856'][@ind2='0'])=0 and count(preceding-sibling::marc:datafield[@tag='856'][@ind2='1'])=0"
								>true</xsl:when>
							<xsl:when
								test="@ind2!='1' and @ind2!='0' and 
								@ind2!='2' and count(ancestor::marc:record//marc:datafield[@tag='856' and 
								@ind2='0'])=0 and count(ancestor::marc:record//marc:datafield[@tag='856' and 
								@ind2='1'])=0 and 
								count(preceding-sibling::marc:datafield[@tag='856'][@ind2])=0"
								>true</xsl:when>
							<xsl:otherwise>false</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:if test="$primary='true'">
						<xsl:attribute name="usage">primary display</xsl:attribute>
					</xsl:if>
					<xsl:if test="marc:subfield[@code='y' or @code='3']">
						<xsl:attribute name="displayLabel">
							<xsl:value-of select="local:subfieldSelect(.,'y3')"/>
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="marc:subfield[@code='z']">
						<xsl:attribute name="note">
							<xsl:value-of select="local:subfieldSelect(.,'z')"/>
						</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="marc:subfield[@code='u']"/>
				</url>
			</location>
		</xsl:if>
	</xsl:template>

	<!-- Related Item templates -->
	<xsl:template match="marc:datafield[@tag='490'][@ind1='0']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="series">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'490')][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<!-- Template checks for altRepGroup - 880 $6 -->
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'a'))"/>
					</title>
					<xsl:if test="marc:subfield[@code='v']">
						<partNumber>
							<xsl:value-of select="local:stripPunctuation(marc:subfield[@code='v'])"
							/>
						</partNumber>
					</xsl:if>
				</titleInfo>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='440']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="series">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'440')][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<!-- Template checks for altRepGroup - 880 $6 -->
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'av'))"
						/>
					</title>
				</titleInfo>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='510']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="isReferencedBy">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'510')][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<xsl:for-each select="marc:subfield[@code='a']">
					<titleInfo>
						<!-- Template checks for altRepGroup - 880 $6 -->
						<xsl:call-template name="xxx880"/>
						<title>
							<xsl:value-of select="."/>
						</title>
					</titleInfo>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='b']">
					<originInfo>
						<!-- Template checks for altRepGroup - 880 $6 -->
						<xsl:call-template name="xxx880"/>
						<dateOther type="coverage">
							<xsl:value-of select="."/>
						</dateOther>
					</originInfo>
				</xsl:for-each>
				<note>
					<!-- Template checks for altRepGroup - 880 $6 -->
					<xsl:call-template name="xxx880"/>
					<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'c'))"/>
				</note>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='534']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="original">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'534')][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<!-- call title template -->
				<xsl:apply-templates select="marc:subfield[@code='t']" mode="relatedItem"/>
				<!-- call name template -->
				<xsl:apply-templates select="marc:subfield[@code='a']" mode="relatedItem"/>
				<!-- orginInfo -->
				<xsl:if test="marc:subfield[@code='b'] or marc:subfield[@code='c']">
					<originInfo>
						<xsl:for-each select="marc:subfield[@code='c']">
							<publisher>
								<xsl:value-of select="local:stripPunctuation(.,':,;/ ')"/>
							</publisher>
						</xsl:for-each>
						<xsl:for-each select="marc:subfield[@code='b']">
							<edition>
								<xsl:apply-templates/>
							</edition>
						</xsl:for-each>
					</originInfo>
				</xsl:if>
				<!-- related item id -->
				<xsl:apply-templates select="marc:subfield[@code='x']" mode="relatedItem"/>
				<xsl:apply-templates select="marc:subfield[@code='z']" mode="relatedItem"/>
				<!-- related item notes -->
				<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItemNote"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='700'][marc:subfield[@code='t']]" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<xsl:if test="@ind2='2'">
				<xsl:attribute name="type">constituent</xsl:attribute>
			</xsl:if>
			<!-- 2.30 -->
			<xsl:if test="marc:subfield[@code='i']">
				<xsl:attribute name="otherType"><xsl:value-of select="marc:subfield[@code='i']"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)][marc:subfield[@code='t']]">
				<titleInfo>
					<xsl:call-template name="xxx7xxt"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'tfklmorsv','t','','g'))"
						/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<name type="personal">
					<xsl:call-template name="xxx7xxn"/>
					<namePart>
						<xsl:value-of select="local:specialSubfieldSelect(.,'aq','t','g','')"/>
					</namePart>
					<xsl:apply-templates select="marc:subfield[@code='b']" mode="termsOfAddress"/>
					<xsl:apply-templates select="marc:subfield[@code='d']" mode="nameDate"/>
					<xsl:apply-templates
						select="marc:subfield[@code='e'] | marc:subfield[@code='4']" mode="role"/>
				</name>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
				<!-- issn -->
				<xsl:apply-templates select="marc:subfield[@code='x']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='710'][marc:subfield[@code='t']]" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<xsl:if test="@ind2='2'">
				<xsl:attribute name="type">constituent</xsl:attribute>
			</xsl:if>
			<!-- 2.30 -->
			<xsl:if test="marc:subfield[@code='i']">
				<xsl:attribute name="otherType"><xsl:value-of select="marc:subfield[@code='i']"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)][marc:subfield[@code='t']]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'tfklmorsv','t','g',''))"
						/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<name type="corporate">
					<xsl:call-template name="xxx880"/>
					<xsl:for-each select="marc:subfield[@code='a']">
						<namePart>
							<xsl:apply-templates/>
						</namePart>
					</xsl:for-each>
					<xsl:for-each select="marc:subfield[@code='b']">
						<namePart>
							<xsl:apply-templates/>
						</namePart>
					</xsl:for-each>
					<xsl:variable name="tempNamePart">
						<xsl:value-of select="local:specialSubfieldSelect(.,'c','t','dgn','')"/>
					</xsl:variable>
					<xsl:if test="normalize-space($tempNamePart)">
						<namePart>
							<xsl:value-of select="$tempNamePart"/>
						</namePart>
					</xsl:if>
					<xsl:apply-templates
						select="marc:subfield[@code='e'] | marc:subfield[@code='4']" mode="role"/>
				</name>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
				<!-- issn -->
				<xsl:apply-templates select="marc:subfield[@code='x']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='711'][marc:subfield[@code='t']]" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<xsl:if test="@ind2='2'">
				<xsl:attribute name="type">constituent</xsl:attribute>
			</xsl:if>
			<!-- 2.30 -->
			<xsl:if test="marc:subfield[@code='i']">
				<xsl:attribute name="otherType"><xsl:value-of select="marc:subfield[@code='i']"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)][marc:subfield[@code='t']]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'tfklsv','t','','g'))"
						/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<name type="conference">
					<xsl:call-template name="xxx880"/>
					<namePart>
						<xsl:value-of select="local:specialSubfieldSelect(.,'aqdc','t','gn','')"/>
					</namePart>
				</name>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
				<!-- issn -->
				<xsl:apply-templates select="marc:subfield[@code='x']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='730'][@ind2='2']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<xsl:if test="@ind2='2'">
				<xsl:attribute name="type">constituent</xsl:attribute>
			</xsl:if>
			<!-- 2.30 -->
			<xsl:if test="marc:subfield[@code='i']">
				<xsl:attribute name="otherType"><xsl:value-of select="marc:subfield[@code='i']"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:subfieldSelect(.,'adfgklmorsv'))"/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
				<!-- issn -->
				<xsl:apply-templates select="marc:subfield[@code='x']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='740'][@ind2='2']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<xsl:if test="@ind2='2'">
				<xsl:attribute name="type">constituent</xsl:attribute>
			</xsl:if>
			<!-- 2.30 -->
			<xsl:if test="marc:subfield[@code='i']">
				<xsl:attribute name="otherType"><xsl:value-of select="marc:subfield[@code='i']"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'730')][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of select="local:stripPunctuation(local:subfieldSelect(.,'a'))"/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='760'] | marc:datafield[@tag='765'] | 
		marc:datafield[@tag='767'] | marc:datafield[@tag='762'] | 
		marc:datafield[@tag='770'] | marc:datafield[@tag='774'] | marc:datafield[@tag='775'] |
		marc:datafield[@tag='772'] | marc:datafield[@tag='773'] |
		marc:datafield[@tag='776'] | marc:datafield[@tag='780'] |
		marc:datafield[@tag='785'] | marc:datafield[@tag='786']"
		mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<!-- selects type attribute -->
			<xsl:choose>
				<xsl:when test="@tag='760'">
					<xsl:attribute name="type">series</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='762' or @tag='770' or @tag='774'">
					<xsl:attribute name="type">constituent</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='765' or @tag='767' or @tag='775'">
					<xsl:attribute name="type">otherVersion</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='772' or @tag='773'">
					<xsl:attribute name="type">host</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='776'">
					<xsl:attribute name="type">otherFormat</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='780'">
					<xsl:attribute name="type">preceding</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='785'">
					<xsl:attribute name="type">succeeding</xsl:attribute>
				</xsl:when>
				<xsl:when test="@tag='786'">
					<xsl:attribute name="type">original</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<!-- selects displayLabel attribute -->
			<xsl:choose>
				<xsl:when test="marc:subfield[@code='i']">
					<xsl:attribute name="otherType">
						<xsl:value-of select="marc:subfield[@code='i']"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="marc:subfield[@code='3']">
					<xsl:attribute name="displayLabel">
						<xsl:value-of select="marc:subfield[@code='3']"/>
					</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<!-- title -->
				<xsl:for-each select="marc:subfield[@code='t']">
					<titleInfo>
						<xsl:call-template name="xxs880"/>
						<title>
							<xsl:value-of select="local:stripPunctuation(.)"/>
						</title>
						<xsl:if test="marc:datafield[@tag!=773]and marc:subfield[@code='g']">
							<xsl:apply-templates select="marc:subfield[@code='g']"
								mode="relatedItem"/>
						</xsl:if>
					</titleInfo>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='p']">
					<titleInfo type="abbreviated">
						<xsl:call-template name="xxs880"/>
						<title>
							<xsl:value-of select="local:stripPunctuation(.)"/>
						</title>
						<xsl:if test="marc:datafield[@tag!=773]and marc:subfield[@code='g']">
							<xsl:apply-templates select="marc:subfield[@code='g']"
								mode="relatedItem"/>
						</xsl:if>
					</titleInfo>
				</xsl:for-each>
				<xsl:for-each select="marc:subfield[@code='s']">
					<titleInfo type="uniform">
						<xsl:call-template name="xxs880"/>
						<title>
							<xsl:value-of select="local:stripPunctuation(.)"/>
						</title>
						<xsl:if test="marc:datafield[@tag!=773]and marc:subfield[@code='g']">
							<xsl:apply-templates select="marc:subfield[@code='g']"
								mode="relatedItem"/>
						</xsl:if>
					</titleInfo>
				</xsl:for-each>

				<!-- orginInfo -->
				<xsl:if test="marc:subfield[@code='b' or @code='d'] or marc:subfield[@code='f']">
					<originInfo>
						<xsl:call-template name="xxx880"/>
						<xsl:if test="@tag='775'">
							<xsl:for-each select="marc:subfield[@code='f']">
								<place>
									<placeTerm>
										<xsl:attribute name="type">code</xsl:attribute>
										<xsl:attribute name="authority">marcgac</xsl:attribute>
										<xsl:value-of select="local:stripPunctuation(.)"/>
									</placeTerm>
								</place>
							</xsl:for-each>
						</xsl:if>
						<xsl:for-each select="marc:subfield[@code='d']">
							<publisher>
								<xsl:value-of select="local:stripPunctuation(.,':,;/ ')"/>
							</publisher>
						</xsl:for-each>
						<xsl:for-each select="marc:subfield[@code='b']">
							<edition>
								<xsl:apply-templates/>
							</edition>
						</xsl:for-each>
					</originInfo>
				</xsl:if>
				<!-- language -->
				<xsl:if test="@tag='775'">
					<xsl:if test="marc:subfield[@code='e']">
						<language>
							<xsl:call-template name="xxx880"/>
							<languageTerm type="code" authority="iso639-2b">
								<xsl:value-of select="marc:subfield[@code='e']"/>
							</languageTerm>
						</language>
					</xsl:if>
				</xsl:if>
				<!-- physical description -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
				<!-- note -->
				<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItemNote"/>
				<!-- subjects -->
				<xsl:apply-templates select="marc:subfield[@code='j']" mode="relatedItem"/>
				<!-- identifiers -->
				<xsl:apply-templates select="marc:subfield[@code='o']" mode="relatedItem"/>
				<xsl:apply-templates select="marc:subfield[@code='x']" mode="relatedItem"/>
				<xsl:apply-templates select="marc:subfield[@code='w']" mode="relatedItem"/>
				<!-- related part -->
				<xsl:if test="@tag='773'">
					<xsl:for-each select="marc:subfield[@code='g']">
						<part>
							<text>
								<xsl:apply-templates/>
							</text>
						</part>
					</xsl:for-each>
					<xsl:for-each select="marc:subfield[@code='q']">
						<part>
							<xsl:call-template name="parsePart"/>
						</part>
					</xsl:for-each>
				</xsl:if>
				<!-- Call names -->
				<xsl:apply-templates select="marc:subfield[@code='a']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='800']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="series">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'tfklmorsv','t','','g'))"
						/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<name type="personal">
					<xsl:call-template name="xxx880"/>
					<namePart>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'aq','t','g',''))"
						/>
					</namePart>
					<xsl:apply-templates select="marc:subfield[@code='b']" mode="termsOfAddress"/>
					<xsl:apply-templates select="marc:subfield[@code='d']" mode="nameDate"/>
					<xsl:apply-templates
						select="marc:subfield[@code='e'] | marc:subfield[@code='4']" mode="role"/>
				</name>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='810']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="series">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'tfklmorsv','t','','dg'))"
						/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<name type="corporate">
					<xsl:call-template name="xxx880"/>
					<xsl:for-each select="marc:subfield[@code='a']">
						<namePart>
							<xsl:apply-templates/>
						</namePart>
					</xsl:for-each>
					<xsl:for-each select="marc:subfield[@code='b']">
						<namePart>
							<xsl:apply-templates/>
						</namePart>
					</xsl:for-each>
					<namePart>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'c','t','dgn',''))"
						/>
					</namePart>
					<xsl:apply-templates
						select="marc:subfield[@code='e'] | marc:subfield[@code='4']" mode="role"/>
				</name>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='811']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="series">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'tfklsv','t','','g'))"
						/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<name type="conference">
					<xsl:call-template name="xxx880"/>
					<namePart>
						<xsl:value-of
							select="local:stripPunctuation(local:specialSubfieldSelect(.,'aqdc','t','gn',''))"
						/>
					</namePart>
					<xsl:apply-templates
						select="marc:subfield[@code='e'] | marc:subfield[@code='4']" mode="role"/>
				</name>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='830']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem type="series">
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<titleInfo>
					<xsl:call-template name="xxx880"/>
					<title>
						<xsl:value-of
							select="local:stripPunctuation(local:subfieldSelect(.,'adfgklmorsv'))"/>
					</title>
					<xsl:apply-templates select="marc:subfield[@code='n']" mode="relatedItem"/>
					<xsl:apply-templates select="marc:subfield[@code='p']" mode="relatedItem"/>
				</titleInfo>
				<!-- physical description form -->
				<xsl:apply-templates select="marc:subfield[@code='h']" mode="relatedItem"/>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='856'][@ind2='2']" mode="relatedItem">
		<xsl:variable name="s6" select="substring(normalize-space(marc:subfield[@code='6']), 5, 2)"/>
		<relatedItem>
			<xsl:for-each
				select=". | ../marc:datafield[@tag='880'][matches(substring(marc:subfield[@code='6'],5,2),$s6)]">
				<xsl:if test="marc:subfield[@code='q']">
					<physicalDescription>
						<xsl:call-template name="xxx880"/>
						<internetMediaType>
							<xsl:value-of select="marc:subfield[@code='q']"/>
						</internetMediaType>
					</physicalDescription>
				</xsl:if>
				<xsl:if test="marc:subfield[@code='u']">
					<location>
						<xsl:call-template name="xxx880"/>
						<url>
							<xsl:if test="marc:subfield[@code='y' or @code='3']">
								<xsl:attribute name="displayLabel">
									<xsl:value-of select="local:subfieldSelect(.,'y3')"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="marc:subfield[@code='z']">
								<xsl:attribute name="note">
									<xsl:value-of select="local:subfieldSelect(.,'z')"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="marc:subfield[@code='u']"/>
						</url>
					</location>
				</xsl:if>
			</xsl:for-each>
		</relatedItem>
	</xsl:template>

	<!-- Related Item common subfields -->
	<!-- Create related item title fields -->
	<xsl:template match="marc:subfield[@code='t']" mode="relatedItem">
		<titleInfo>
			<xsl:call-template name="xxs880"/>
			<title>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</title>
			<!-- call part numbers -->
			<xsl:apply-templates select="following-sibling::marc:subfield[@code='n']"
				mode="relatedItem"/>
			<xsl:apply-templates select="../marc:subfield[@code='p']" mode="relatedItem"/>
			<xsl:apply-templates select="../marc:subfield[@code='g']" mode="relatedItem"/>
		</titleInfo>
	</xsl:template>
	<!-- Create related item title part number -->
	<xsl:template match="marc:subfield[@code='n']" mode="relatedItem">
		<xsl:choose>
			<xsl:when
				test="parent::marc:datafield[@tag='710' or @tag='810' or @tag='711' or @tag='811']">
				<xsl:if test="preceding-sibling::marc:subfield[@code='t']">
					<partNumber>
						<xsl:value-of select="local:stripPunctuation(.)"/>
					</partNumber>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<partNumber>
					<xsl:value-of select="local:stripPunctuation(.)"/>
				</partNumber>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="marc:subfield[@code='g']" mode="relatedItem">
		<xsl:if
			test="parent::marc:datafield[@tag='760' or @tag='762' or @tag='765' 
			or @tag='767' or @tag='770' or @tag='772' or @tag='774' or @tag='775'
			or @tag='776' or @tag='777' or @tag='780' or @tag='780' or @tag='785' or
			@tag='786' or @tag='787']">
			<partNumber>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</partNumber>
		</xsl:if>
	</xsl:template>
	<!-- Create related item title part name -->
	<xsl:template match="marc:subfield[@code='p']" mode="relatedItem">
		<!-- NOTE: old stylesheet outputs code p for 740, mapping does not indicate this -->
		<xsl:if test="parent::marc:datafield[@tag='773' or @tag='786']">
			<partName>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</partName>
		</xsl:if>
	</xsl:template>
	<!-- Creates related item names -->
	<xsl:template match="marc:subfield[@code='a']" mode="relatedItem">
		<name>
			<xsl:call-template name="xxs880"/>
			<namePart>
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</namePart>
		</name>
	</xsl:template>
	<!-- Creates related item id -->
	<xsl:template match="marc:subfield[@code='x']" mode="relatedItem">
		<identifier type="issn">
			<xsl:apply-templates/>
		</identifier>
	</xsl:template>
	<xsl:template match="marc:subfield[@code='z']" mode="relatedItem">
		<identifier type="isbn">
			<xsl:apply-templates/>
		</identifier>
	</xsl:template>
	<xsl:template match="marc:subfield[@code='w']" mode="relatedItem">
		<identifier type="local">
			<xsl:apply-templates/>
		</identifier>
	</xsl:template>
	<xsl:template match="marc:subfield[@code='o']" mode="relatedItem">
		<identifier>
			<xsl:apply-templates/>
		</identifier>
	</xsl:template>
	<!-- Creates related item notes -->
	<xsl:template match="marc:subfield[@code='n']" mode="relatedItemNote">
		<note>
			<xsl:call-template name="xxs880"/>
			<xsl:value-of select="."/>
		</note>
	</xsl:template>
	<!-- Creates related item form -->
	<xsl:template match="marc:subfield[@code='h']" mode="relatedItem">
		<physicalDescription>
			<xsl:call-template name="xxs880"/>
			<form>
				<xsl:apply-templates/>
			</form>
		</physicalDescription>
	</xsl:template>
	<!-- Creates related item subjects -->
	<xsl:template match="marc:subfield[@code='j']" mode="relatedItem">
		<subject>
			<xsl:call-template name="xxs880"/>
			<temporal encoding="iso8601">
				<xsl:value-of select="local:stripPunctuation(.)"/>
			</temporal>
		</subject>
	</xsl:template>

	<!-- Create Identifiers -->
	<xsl:template
		match="marc:datafield[@tag='020'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'020')]"
		mode="id">
		<identifier>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<!-- Add attribute type -->
			<xsl:if test="marc:subfield[@code='a' or @code='z']">
				<xsl:attribute name="type">isbn</xsl:attribute>
			</xsl:if>
			<!-- Add attribute code -->
			<xsl:if test="marc:subfield[@code='z']">
				<xsl:attribute name="invalid">yes</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="local:subfieldSelect(.,'az')"/>
		</identifier>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='024'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'024')]"
		mode="id">
		<xsl:if test="marc:subfield[@code='a' or @code='2']">
			<identifier>
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<!-- create invalid attribute -->
				<xsl:if test="marc:subfield[@code='z']">
					<xsl:attribute name="invalid">yes</xsl:attribute>
				</xsl:if>
				<!-- create type attribute -->
				<xsl:choose>
					<xsl:when test="@ind1='0'">
						<xsl:attribute name="type">isrc</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind1='2'">
						<xsl:attribute name="type">ismn</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind1='7' and marc:subfield[@code='2']">
						<xsl:attribute name="type">
							<xsl:value-of select="marc:subfield[@code='2']"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind1='4'">
						<xsl:attribute name="type">sici</xsl:attribute>
					</xsl:when>
					<xsl:when test="@ind1='1'">
						<xsl:attribute name="type">upc</xsl:attribute>
					</xsl:when>
				</xsl:choose>
				<!-- call subfield a-->
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</identifier>
		</xsl:if>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='022']" mode="id">
		<xsl:if test="marc:subfield[@code='a']">
			<identifier type="issn">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="marc:subfield[@code='a']"/>
			</identifier>
		</xsl:if>
		<xsl:if test="marc:subfield[@code='z']">
			<identifier type="issn" invalid="yes">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="marc:subfield[@code='z']"/>
			</identifier>
		</xsl:if>
		<xsl:if test="marc:subfield[@code='y']">
			<identifier type="issn" invalid="yes">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="marc:subfield[@code='y']"/>
			</identifier>
		</xsl:if>
		<xsl:if test="marc:subfield[@code='l']">
			<identifier type="issn-l">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="marc:subfield[@code='l']"/>
			</identifier>
		</xsl:if>
		<xsl:if test="marc:subfield[@code='m']">
			<identifier type="issn-l" invalid="yes">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="marc:subfield[@code='m']"/>
			</identifier>
		</xsl:if>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='010'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'010')]"
		mode="id">
		<xsl:for-each select="marc:subfield[@code='a']">
			<identifier type="lccn">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="normalize-space(.)"/>
			</identifier>
		</xsl:for-each>
		<xsl:for-each select="marc:subfield[@code='z']">
			<identifier type="lccn" invalid="yes">
				<!-- Template checks for altRepGroup - 880 $6 -->
				<xsl:call-template name="xxx880"/>
				<xsl:value-of select="normalize-space(.)"/>
			</identifier>
		</xsl:for-each>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='028'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'028')]"
		mode="id">
		<identifier>
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:attribute name="type">
				<xsl:choose>
					<xsl:when test="@ind1='0'">issue number</xsl:when>
					<xsl:when test="@ind1='1'">matrix number</xsl:when>
					<xsl:when test="@ind1='2'">music plate</xsl:when>
					<xsl:when test="@ind1='3'">music publisher</xsl:when>
					<xsl:when test="@ind1='4'">videorecording identifier</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="@ind1='0'">
					<xsl:value-of select="local:subfieldSelect(.,'ba')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
				</xsl:otherwise>
			</xsl:choose>
		</identifier>
	</xsl:template>
	<xsl:template match="marc:datafield[@tag='035']" mode="id">
		<xsl:for-each select="marc:subfield[@code='a'][contains(., '(OCoLC)')]">
			<identifier type="oclc">
				<xsl:value-of select="normalize-space(substring-after(., '(OCoLC)'))"/>
			</identifier>
		</xsl:for-each>
		<!-- 3.5 2.21 20140421 -->
		<xsl:for-each select="marc:datafield[@tag='035'][marc:subfield[@code='a'][contains(text(), '(WlCaITV)')]]">
			<identifier type="WlCaITV">
				<xsl:value-of select="normalize-space(substring-after(marc:subfield[@code='a'], '(WlCaITV)'))"/>
			</identifier>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template
		match="marc:datafield[@tag='037'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'037')]"
		mode="id">
		<identifier type="stock number">
			<!-- Template checks for altRepGroup - 880 $6 -->
			<xsl:call-template name="xxx880"/>
			<xsl:if test="marc:subfield[@code='c']">
				<xsl:attribute name="displayLabel">
					<xsl:value-of select="marc:subfield[@code='c']"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="local:subfieldSelect(.,'ab')"/>
		</identifier>
	</xsl:template>
	<xsl:template
		match="marc:datafield[@tag='856']/marc:subfield[@code='u'] | marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'856')]/marc:subfield[@code='u']"
		mode="id">
		<xsl:choose>
			<xsl:when
				test="starts-with(.,'urn:hdl') or starts-with(.,'hdl') or starts-with(.,'http://hdl.loc.gov') ">
				<identifier>
					<!-- Template checks for altRepGroup - 880 $6 -->
					<xsl:call-template name="xxx880"/>
					<xsl:attribute name="type">
						<xsl:choose>
							<xsl:when test="starts-with(.,'urn:doi') or starts-with(.,'doi')"
								>doi</xsl:when>
							<xsl:when
								test="starts-with(.,'urn:hdl') or starts-with(.,'hdl') or starts-with(.,'http://hdl.loc.gov')"
								>hdl</xsl:when>
						</xsl:choose>
					</xsl:attribute>
					<xsl:value-of select="concat('hdl:',substring-after(.,'http://hdl.loc.gov/'))"/>
				</identifier>
			</xsl:when>
			<xsl:when test="starts-with(.,'urn:hdl') or starts-with(.,'hdl')">
				<identifier type="hdl">
					<!-- Template checks for altRepGroup - 880 $6 -->
					<xsl:call-template name="xxx880"/>
					<xsl:if test="marc:subfield[@code='y' or @code='3' or @code='z']">
						<xsl:value-of select="local:subfieldSelect(parent::*,'y3z')"/>
					</xsl:if>
					<xsl:value-of select="concat('hdl:',.,'http://hdl.loc.gov/')"/>
				</identifier>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- 880 processing -->
	<xsl:template name="xxx880">
		<!-- Checks for subfield $6 ands linking data -->
		<xsl:if test="child::marc:subfield[@code='6']">
			<xsl:variable name="sf06" select="normalize-space(child::marc:subfield[@code='6'])"/>
			<xsl:variable name="sf06b" select="substring($sf06, 5, 2)"/>
			<xsl:variable name="scriptCode" select="substring($sf06, 8, 2)"/>
			<xsl:attribute name="altRepGroup">
				<xsl:value-of select="$sf06b"/>
			</xsl:attribute>
			<xsl:call-template name="scriptCode"/>
		</xsl:if>
	</xsl:template>
	<!-- 880 processing when called from subfield -->
	<xsl:template name="xxs880">
		<!-- Checks for subfield $6 ands linking data -->
		<xsl:if test="preceding-sibling::*[@code='6']">
			<xsl:variable name="sf06" select="normalize-space(preceding-sibling::*[@code='6'])"/>
			<xsl:variable name="sf06b" select="substring($sf06, 5, 2)"/>
			<xsl:variable name="scriptCode" select="substring($sf06, 8, 2)"/>
			<xsl:attribute name="altRepGroup">
				<xsl:value-of select="$sf06b"/>
			</xsl:attribute>
			<xsl:attribute name="script">
				<xsl:choose>
					<xsl:when test="$scriptCode=''">Latn</xsl:when>
					<xsl:when test="$scriptCode='(3'">Arab</xsl:when>
					<xsl:when test="$scriptCode='(4'">Arab</xsl:when>
					<xsl:when test="$scriptCode='(B'">Latn</xsl:when>
					<xsl:when test="$scriptCode='!E'">Latn</xsl:when>
					<xsl:when test="$scriptCode='$1'">CJK</xsl:when>
					<xsl:when test="$scriptCode='(N'">Cyrl</xsl:when>
					<xsl:when test="$scriptCode='(Q'">Cyrl</xsl:when>
					<xsl:when test="$scriptCode='(2'">Hebr</xsl:when>
					<xsl:when test="$scriptCode='(S'">Grek</xsl:when>
				</xsl:choose>
			</xsl:attribute>
		</xsl:if>
	</xsl:template>
	<xsl:template name="xxx7xxt">
		<!-- Checks for subfield $6 ands linking data -->
		<xsl:if test="child::marc:subfield[@code='6']">
			<xsl:variable name="sf06" select="normalize-space(child::marc:subfield[@code='6'])"/>
			<xsl:variable name="sf06b" select="substring($sf06, 5, 2)"/>
			<xsl:variable name="scriptCode" select="substring($sf06, 8, 2)"/>
			<xsl:attribute name="altRepGroup">
				<xsl:value-of select="$sf06b"/>
			</xsl:attribute>
			<xsl:call-template name="scriptCode"/>
		</xsl:if>
		<xsl:if test="child::marc:subfield[@code='t'] and child::marc:subfield[@code='a']">
			<xsl:call-template name="nameTitleGroup"/>
		</xsl:if>
	</xsl:template>
	<xsl:template name="xxx7xxn">
		<!-- Checks for subfield $6 ands linking data -->
		<xsl:if test="child::marc:subfield[@code='6']">
			<xsl:variable name="sf06" select="normalize-space(child::marc:subfield[@code='6'])"/>
			<xsl:variable name="sf06b" select="substring($sf06, 5, 2)"/>
			<xsl:variable name="scriptCode" select="substring($sf06, 8, 2)"/>
			<xsl:attribute name="altRepGroup">
				<xsl:value-of select="concat($sf06b,$sf06b)"/>
			</xsl:attribute>
			<xsl:call-template name="scriptCode"/>
		</xsl:if>
		<xsl:if test="child::marc:subfield[@code='t'] and child::marc:subfield[@code='a']">
			<xsl:call-template name="nameTitleGroup"/>
		</xsl:if>
	</xsl:template>

	<!-- rules for applying nameTitleGroup attribute -->
	<xsl:template name="nameTitleGroup">
		<xsl:choose>
			<xsl:when test="self::marc:datafield[@tag='240']">
				<xsl:choose>
					<xsl:when test="../marc:datafield[@tag='100' or @tag='110' or @tag='111']">
						<xsl:attribute name="nameTitleGroup">1</xsl:attribute>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:when>
			<xsl:when
				test="self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')]">
				<xsl:choose>
					<xsl:when
						test="../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'100')] or 
						../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'110')] or
						../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'111')]">
						<xsl:attribute name="nameTitleGroup">
							<xsl:value-of
								select="count(preceding-sibling::marc:datafield[@tag='700' or @tag='710' or @tag='711' or @tag='880']) + 2"
							/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="self::marc:datafield[@tag='100' or @tag='110' or @tag='111']">
				<xsl:choose>
					<xsl:when test="../marc:datafield[@tag='240']">
						<xsl:attribute name="nameTitleGroup">1</xsl:attribute>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:when>
			<xsl:when
				test="(self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'100')]
				or self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'110')]
				or self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'111')])">
				<xsl:choose>
					<xsl:when
						test="../marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'240')]">
						<xsl:attribute name="nameTitleGroup">
							<xsl:value-of
								select="count(preceding-sibling::marc:datafield[@tag='700' or @tag='710' or @tag='711' or @tag='880']) + 2"
							/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="self::marc:datafield[@tag='700' or @tag='710' or @tag='711']">
				<xsl:choose>
					<xsl:when test="child::marc:subfield[@code='t']">
						<xsl:attribute name="nameTitleGroup">
							<xsl:value-of
								select="count(preceding-sibling::marc:datafield[@tag='700' or @tag='710' or @tag='711' or @tag='880']) + 2"
							/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:when>
			<xsl:when
				test="self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'100')] 
				| self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'110')] 
				| self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'111')] "/>
			<xsl:when
				test="self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'700')][not(marc:subfield[@code='t'])]"/>
			<xsl:when
				test="self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'710')][not(marc:subfield[@code='t'])]"/>
			<xsl:when
				test="self::marc:datafield[@tag='880'][starts-with(marc:subfield[@code='6'],'711')][not(marc:subfield[@code='t'])]"/>
			<xsl:otherwise>
				<xsl:attribute name="nameTitleGroup">
					<xsl:value-of
						select="count(preceding-sibling::marc:datafield[@tag='700' or @tag='710' or @tag='711' or @tag='880']) + 2"
					/>
				</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Script code template -->
	<xsl:template name="scriptCode">
		<xsl:variable name="sf06" select="normalize-space(marc:subfield[@code='6'])"/>
		<xsl:variable name="scriptCode" select="substring($sf06, 8, 2)"/>
		<xsl:if test="//marc:datafield/marc:subfield[@code='6']">
			<xsl:attribute name="script">
				<xsl:choose>
					<xsl:when test="$scriptCode=''">Latn</xsl:when>
					<xsl:when test="$scriptCode='(3'">Arab</xsl:when>
					<xsl:when test="$scriptCode='(4'">Arab</xsl:when>
					<xsl:when test="$scriptCode='(B'">Latn</xsl:when>
					<xsl:when test="$scriptCode='!E'">Latn</xsl:when>
					<xsl:when test="$scriptCode='$1'">CJK</xsl:when>
					<xsl:when test="$scriptCode='(N'">Cyrl</xsl:when>
					<xsl:when test="$scriptCode='(Q'">Cyrl</xsl:when>
					<xsl:when test="$scriptCode='(2'">Hebr</xsl:when>
					<xsl:when test="$scriptCode='(S'">Grek</xsl:when>
				</xsl:choose>
			</xsl:attribute>
		</xsl:if>
	</xsl:template>

	<!-- Named template used by relatedItem-->
	<xsl:template name="parsePart">
		<!-- assumes 773$q= 1:2:3<4 with up to 3 levels and one optional start page -->
		<xsl:variable name="level1">
			<xsl:choose>
				<xsl:when test="contains(text(),':')">
					<!-- 1:2 -->
					<xsl:value-of select="substring-before(text(),':')"/>
				</xsl:when>
				<xsl:when test="not(contains(text(),':'))">
					<!-- 1 or 1<3 -->
					<xsl:if test="contains(text(),'&lt;')">
						<!-- 1<3 -->
						<xsl:value-of select="substring-before(text(),'&lt;')"/>
					</xsl:if>
					<xsl:if test="not(contains(text(),'&lt;'))">
						<!-- 1 -->
						<xsl:value-of select="text()"/>
					</xsl:if>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="sici2">
			<xsl:choose>
				<xsl:when test="starts-with(substring-after(text(),$level1),':')">
					<xsl:value-of select="substring(substring-after(text(),$level1),2)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="substring-after(text(),$level1)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="level2">
			<xsl:choose>
				<xsl:when test="contains($sici2,':')">
					<!--  2:3<4  -->
					<xsl:value-of select="substring-before($sici2,':')"/>
				</xsl:when>
				<xsl:when test="contains($sici2,'&lt;')">
					<!-- 1: 2<4 -->
					<xsl:value-of select="substring-before($sici2,'&lt;')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$sici2"/>
					<!-- 1:2 -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="sici3">
			<xsl:choose>
				<xsl:when test="starts-with(substring-after($sici2,$level2),':')">
					<xsl:value-of select="substring(substring-after($sici2,$level2),2)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="substring-after($sici2,$level2)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="level3">
			<xsl:choose>
				<xsl:when test="contains($sici3,'&lt;')">
					<!-- 2<4 -->
					<xsl:value-of select="substring-before($sici3,'&lt;')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$sici3"/>
					<!-- 3 -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="page">
			<xsl:if test="contains(text(),'&lt;')">
				<xsl:value-of select="substring-after(text(),'&lt;')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="$level1">
			<detail level="1">
				<number>
					<xsl:value-of select="$level1"/>
				</number>
			</detail>
		</xsl:if>
		<xsl:if test="$level2">
			<detail level="2">
				<number>
					<xsl:value-of select="$level2"/>
				</number>
			</detail>
		</xsl:if>
		<xsl:if test="$level3">
			<detail level="3">
				<number>
					<xsl:value-of select="$level3"/>
				</number>
			</detail>
		</xsl:if>
		<xsl:if test="$page">
			<extent unit="page">
				<start>
					<xsl:value-of select="$page"/>
				</start>
			</extent>
		</xsl:if>
	</xsl:template>

	<!-- Generates xlink:href attribute -->
	<xsl:template name="uri">
		<xsl:for-each select="marc:subfield[@code='u']|marc:subfield[@code='0']">
			<xsl:attribute name="xlink:href">
				<xsl:apply-templates/>
			</xsl:attribute>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
