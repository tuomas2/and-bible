package net.bible.service.format.osistohtml.speak_taghandler;

import net.bible.service.format.osistohtml.HtmlTextWriter;
import net.bible.service.format.osistohtml.OsisToHtmlParameters;
import net.bible.service.format.osistohtml.osishandlers.OsisToHtmlSaxHandler
import net.bible.service.format.osistohtml.taghandler.OsisTagHandler;
import net.bible.service.format.osistohtml.taghandler.TagHandlerHelper;
import org.crosswire.jsword.book.OSISUtil;
import org.xml.sax.Attributes;

import java.util.Arrays;
import java.util.Stack;

class DivHandler (val parameters: OsisToHtmlParameters, val verseInfo: OsisToHtmlSaxHandler.VerseInfo, val passageInfo: OsisToHtmlSaxHandler.PassageInfo,
				  val writer: HtmlTextWriter): OsisTagHandler {
	private enum class DivType {PARAGRAPH, PREVERSE, PREVERSE_START_MILESTONE, PREVERSE_END_MILESTONE, IGNORE}

	private val stack: Stack<DivType> = Stack()

	private val PARAGRAPH_TYPE_LIST: MutableList<String> = Arrays.asList("paragraph", "x-p", "x-end-paragraph")

	override fun getTagName() : String {
        return OSISUtil.OSIS_ELEMENT_DIV;
    }

	override fun start(attrs: Attributes) {
		var divType = DivType.IGNORE;
		var type = attrs.getValue("type");
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
