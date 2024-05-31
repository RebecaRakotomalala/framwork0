package mg.itu.prom16;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontController extends HttpServlet {
    private final Map<String, List<Mapping>> urlMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        scanControllers(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>FrontController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1 style='color:blue'>URL actuelle :</h1>");
            out.println("<p>" + request.getRequestURL() + "</p>");

            String path = request.getPathInfo();
            if (path == null) {
                path = "/";
            } else if (!path.startsWith("/")) {
                path = "/" + path;
            }

            List<Mapping> matchedMappings = urlMapping.getOrDefault(path, new ArrayList<>());

            if (!matchedMappings.isEmpty()) {
                out.println("<h2>Liste des contrôleurs et leurs méthodes annotées :</h2>");
                out.println("<p>URL: " + path + "</p>");
                for (Mapping mapping : matchedMappings) {
                    out.println("<p>Classe: " + mapping.getControllerClass().getName() + "</p>");
                    out.println("<p>Méthode: " + mapping.getMethod().getName() + "</p>");
                    try {
                        Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                        Object result = mapping.getMethod().invoke(controllerInstance);
                        out.println("<p>Valeur de retour: " + (result != null ? result.toString() : "null") + "</p>");
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        out.println("<p style='color:red'>Erreur lors de l'invocation de la méthode: " + e.getMessage() + "</p>");
                    }
                    out.println("<hr>");
                }
            } else {
                out.println("<h2 style='color:red'>Aucun mapping trouvé pour l'URL : " + path + "</h2>");
            }
            out.println("</body>");
            out.println("</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanControllers(ServletConfig config) {
        String controllerPackage = config.getInitParameter("controller-package");
        System.out.println("Scanning package: " + controllerPackage);

        try {
            String path = "WEB-INF/classes/" + controllerPackage.replace('.', '/');
            File directory = new File(getServletContext().getRealPath(path));
            if (directory.exists()) {
                scanDirectory(directory, controllerPackage);
            } else {
                System.out.println("Directory does not exist: " + directory.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanDirectory(File directory, String packageName) {
        System.out.println("Scanning directory: " + directory.getAbsolutePath());

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(AnnotationController.class)) {
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(AnnotationMethode.class)) {
                                AnnotationMethode requestMapping = method.getAnnotation(AnnotationMethode.class);
                                String urlKey = requestMapping.value();
                                if (!urlKey.startsWith("/")) {
                                    urlKey = "/" + urlKey;
                                }
                                urlMapping.putIfAbsent(urlKey, new ArrayList<>());
                                urlMapping.get(urlKey).add(new Mapping(urlKey, clazz, method));
                                System.out.println("Mapped URL: " + urlKey + " to " + clazz.getName() + "." + method.getName());
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
