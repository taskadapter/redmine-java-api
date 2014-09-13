package com.taskadapter.redmineapi;

public enum Include {
        // these values MUST BE exactly as they are written here,
        // can't use capital letters or rename.
        // they are provided in "?include=..." HTTP request
        journals, relations, attachments, changesets, watchers
}
