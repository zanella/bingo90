package zane.raf.bingo_90;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@Path("/bingo90")
public class Bingo90 {
    /**
     * Generates a valid strip, consisting of 6 tickets
     *
     * @return a valid {@code Strip}
     */
    @GET
    @Path("/api/strip")
    @Produces(MediaType.APPLICATION_JSON)
    public Strip generateStrip() {
        return Strip.create();
    }

    ///////////////////////////////////////////////////////////////////////////

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance bingo90IndexTemplate();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return Templates.bingo90IndexTemplate();
    }

    ///////////////////////////////////////////////////////////////////////////

    Bingo90() {}
}
