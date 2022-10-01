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

    @GET
    @Path("/api/10k")
    @Produces(MediaType.APPLICATION_JSON)
    public Long generate10kStrip() {
        final var start = System.currentTimeMillis();

        for (int i = 0; i < 10_000; i++) { Strip.create(); }

        final var elapsed = System.currentTimeMillis() - start;

        System.out.println("Elapsed: " + elapsed);

        return elapsed;
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
