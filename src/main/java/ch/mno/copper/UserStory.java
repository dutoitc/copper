package ch.mno.copper;

/**
 *
 * Vocabulary: (space means space and newline)
 * <ul>
 *     <li>DEF_DEFINE::="DEFINE" KEY VALUE EOL (value could be multiline)</li>
 *     <li>KEY::=\w</li>
 *     <li>VALUE::=\w</li>
 *     <li>GIVEN::="GIVEN" COLLECTOR</li>
 *     //
 *     //
 *     //
 *     //
 *     <li>COLLECTOR::="COLLECTOR" COLLECTOR_ORACLE | ...</li>
 *     <li>COLLECTOR_ORACLE::="ORACLE WITH url=" W ",user=" W ",password=" W
 *     //
 *     //
 *     //
 *     //
 *     <li>STORE::="STORE_VALUES" ("WITH" "prefix="\w)</li>
 *     //
 *     //
 *     //
 *     //
 *     <li>WHEN_CRON::="CRON" W W W W W</li>
 *     <li></li>
 *     <li>WHEN::=WHEN_CRON | ("WHEN CHANGED")</li>
 *     //
 *     //
 *     //
 *     //
 *     <li>REPORT_MAIL::="REPORT_BY_MAIL to" W " WITH title=\"" W+ "\",body=\"" W+ "\""</li>
 *     <li>REPORT::=REPORT_MAIL | ...</li>
 *     //
 *     //
 *     //
 *     //
 *     <li>MAIN::=GIVEN WHEN (STORE | REPORT)</li>
 *     //
 *     //
 *     //
 *     //
 *     <li>W::=(word)</li>
 * </ul>
 *
 *
 * Created by xsicdt on 05/02/16.
 */
public class UserStory {

    public UserStory(String content) {

    }

}