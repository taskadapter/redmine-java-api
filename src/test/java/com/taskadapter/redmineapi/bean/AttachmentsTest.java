package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.internal.RedmineJSONBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AttachmentsTest {

    @Test
    public void wikiPageDetailWrite() {
        Attachment attachment1 = new Attachment(null).setId(1)
                .setToken("TOKEN1")
                .setContentType("text/plain");
        Attachment attachment2 = new Attachment(null).setId(2)
                .setToken("TOKEN2")
                .setContentType("text/plain");
        List<Attachment> attachments = Arrays.asList(attachment1, attachment2);

        WikiPageDetail wikiPageDetail = new WikiPageDetail(null);
        wikiPageDetail.setText("text");
        wikiPageDetail.setAttachments(attachments);

        final String generatedJSON = RedmineJSONBuilder.toSimpleJSON("some_project_key", wikiPageDetail, RedmineJSONBuilder::writeWikiPageDetail);
        assertThat(generatedJSON).contains("\"text\":\"text\"");
        assertThat(generatedJSON).contains("\"uploads\":[{\"token\":\"TOKEN1\",\"content_type\":\"text/plain\"},{\"token\":\"TOKEN2\",\"content_type\":\"text/plain\"}]}}");
    }

}
