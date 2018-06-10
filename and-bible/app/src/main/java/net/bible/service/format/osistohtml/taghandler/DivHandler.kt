package net.bible.service.format.osistohtml.taghandler;

/** This can either signify a quote or Red Letter
 * Example from ESV Prov 19:1
 * 		<l sID="x9938"/>...<l eID="x9938" type="x-br"/><l sID="x9939" type="x-indent"/>..<l eID="x9939" type="x-br"/>
 * 
 * Apparently quotation marks are not supposed to appear in the KJV (https://sites.google.com/site/kjvtoday/home/Features-of-the-KJV/quotation-marks)
 * 
 * http://www.crosswire.org/wiki/List_of_eXtensions_to_OSIS_used_in_SWORD
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author. 
 */

import net.bible.service.format.osistohtml.HtmlTextWriter;
import net.bible.service.format.osistohtml.OsisToHtmlParameters;
import net.bible.service.format.osistohtml.osishandlers.OsisToHtmlSaxHandler.PassageInfo;
import net.bible.service.format.osistohtml.osishandlers.OsisToHtmlSaxHandler.VerseInfo;
import org.crosswire.jsword.book.OSISUtil;
import org.xml.sax.Attributes;

import java.util.Arrays;
import java.util.Stack;

class DivHandler (val parameters: OsisToHtmlParameters, val verseInfo: VerseInfo, val passageInfo: PassageInfo,
				  val writer: HtmlTextWriter): OsisTagHandler {
	private enum class DivType {PARAGRAPH, PREVERSE, PREVERSE_START_MILESTONE, PREVERSE_END_MILESTONE, IGNORE}

	private val stack: Stack<DivType> = Stack()

	private val PARAGRAPH_TYPE_LIST: MutableList<String> = Arrays.asList("paragraph", "x-p", "x-end-paragraph")

	override fun getTagName() : String {
		return OSISUtil.OSIS_ELEMENT_DIV;
	}

	override fun start(attrs: Attributes) {
		var divType = DivType.IGNORE;
		val type = attrs.getValue("type");
		if (PARAGRAPH_TYPE_LIST.contains(type)) {
			// ignore sID start paragraph sID because it often comes after the verse no and causes a gap between verse no verse text
			// could enhance this to use writeOptionallyBeforeVerse('<p>') and then write </p> in end() if there is no sID or eID
			val sID = attrs.getValue("sID");
			if (sID==null) {
				divType = DivType.PARAGRAPH;
			}
		} else if (TagHandlerHelper.contains(OSISUtil.OSIS_ATTR_SUBTYPE, attrs, "preverse")) {
			if (TagHandlerHelper.isAttr(OSISUtil.OSIS_ATTR_SID, attrs)) {
				divType = DivType.PREVERSE_START_MILESTONE;
				writer.beginInsertAt(verseInfo.positionToInsertBeforeVerse);

			} else if (TagHandlerHelper.isAttr(OSISUtil.OSIS_ATTR_EID, attrs)) {
				divType = DivType.PREVERSE_END_MILESTONE;
				writer.finishInserting();

			} else {
				divType = DivType.PREVERSE;
				writer.beginInsertAt(verseInfo.positionToInsertBeforeVerse);
			}
		}
		stack.push(divType);
	}

	override fun end() {
		val type = stack.pop();
		if (DivType.PARAGRAPH.equals(type) && passageInfo.isAnyTextWritten) {
			writer.write("<div class='breakline'></div>");
		} else if (DivType.PREVERSE.equals(type)) {
			writer.finishInserting();
		}
	}
}
