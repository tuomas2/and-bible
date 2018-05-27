package net.bible.service.format.osistohtml.osishandlers

import org.crosswire.jsword.book.OSISUtil
import org.xml.sax.Attributes

class OsisToSpeakTextSaxHandler(private val sayReferences: Boolean): OsisToCanonicalTextSaxHandler() {
    private var writingRef: Boolean = false

    override fun startElement(namespaceURI: String?, sName: String?, qName: String?, attrs: Attributes?) {
        val name = getName(sName, qName)
        if(sayReferences && name.equals(OSISUtil.OSIS_ELEMENT_REFERENCE)) {
            writeContent(true)
            writingRef = true;
        }
        else {
            super.startElement(namespaceURI, sName, qName, attrs)
        }
    }

    override fun endElement(namespaceURI: String?, sName: String?, qName: String?) {
        val name = getName(sName, qName)
        if (sayReferences && name.equals(OSISUtil.OSIS_ELEMENT_REFERENCE)) {
            writingRef = false
        }
        super.endElement(namespaceURI, sName, qName)
    }
    override fun write(inString: String) {
        var s = inString
    	// NetText often uses single quote where esv uses double quote and TTS says open single quote e.g. Matt 4
    	// so replace all single quotes with double quotes but only if they are used for quoting text as in e.g. Ps 117
    	// it is tricky to distinguish single quotes from apostrophes and this won't work all the time

    	if (s.contains(" \'") || s.startsWith("\'")) {
    		s = s.replace("\'", "\"");
    	}
    	// Finney Gospel Sermons contains to many '--'s which are pronounced as hyphen hyphen
    	if (s.contains(" --")) {
    		s = s.replace(" --", ";");
    	}

   		// for xxx's TTS says xxx s instead of xxxs so remove possessive apostrophe
   		s = s.replace("\'s ", "s ");

		// say verse rather than colon etc.
		if (writingRef) {
			s = s.replace(":", " verse ").replace("-", " to ");
		}

		super.write(s);
    }
}