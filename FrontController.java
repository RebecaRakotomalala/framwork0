package mg.itu.prom16;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

public class FrontController extends HttpServlet {
    private boolean checked = false;
    private List<String> listeControllers = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        scanControllers();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    private synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>FrontController</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>URL actuelle :</h1>");
        out.println("<p>" + request.getRequestURL() + "</p>");

        if (!checked) {
            scanControllers();
            checked = true;
        }

        out.println("<h2>Liste des contrôleurs annotés avec @AnnotationController :</h2>");
        for (String controller : listeControllers) {
            out.println("<p>" + controller + "</p>");
        }

        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private void scanControllers() {
        ServletConfig config = getServletConfig();
        String controllerPackage = config.getInitParameter("controller-package");
    
        // Utilisez Reflections pour scanner le package et récupérer les classes annotées avec @AnnotationController
        Reflections reflections = new Reflections(controllerPackage);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(AnnotationController.class);
    
        // Parcourez chaque classe annotée pour récupérer les noms des classes
        for (Class<?> clazz : annotatedClasses) {
            // Vérifiez si la classe est annotée avec @AnnotationController
            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                // Si oui, ajoutez le nom de la classe à la liste des contrôleurs
                listeControllers.add(clazz.getName());
            }
        }
    }
    
}
