package mg.itu.prom16;

import mg.itu.prom16.AnnotationController;
import mg.itu.prom16.GetAnnotation;
import mg.itu.prom16.Post;
import mg.itu.prom16.Param;
import mg.itu.prom16.CustomSession;
import mg.itu.prom16.Restapi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;

import mg.itu.prom16.ModelView; 
import mg.itu.prom16.RequestBody;
import java.lang.reflect.Field;  
import java.io.*;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import jakarta.servlet.RequestDispatcher; 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.Gson;

public class FrontController extends HttpServlet {
    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    boolean checked = false;
    HashMap<String, Mapping> lien = new HashMap<>();
    String error = "";

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        try {
            this.scan();
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        
        // Récupérer l'URL et le verbe (GET/POST)
        String[] requestUrlSplitted = request.getRequestURL().toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];
        String verb = request.getMethod();  // GET ou POST

        // Vérifier s'il y a des erreurs
        if (!error.isEmpty()) {
            out.println(error);
            out.close();
            return;
        }

        // Vérifier si l'URL est enregistrée avec le bon verbe (GET ou POST)
        if (!lien.containsKey(controllerSearched + "-" + verb)) {
            out.println("<p>Méthode non trouvée pour ce verbe (" + verb + ").</p>");
            out.close();
            return;
        }

        try {
            // Récupérer le mapping (classe et méthode) en fonction de l'URL et du verbe
            Mapping mapping = lien.get(controllerSearched + "-" + verb);
            Class<?> clazz = Class.forName(mapping.getClassName());
            
            // Récupérer la méthode spécifiée dans le mapping
            Method method = clazz.getDeclaredMethod(mapping.getMethodeName());

            // Injection des paramètres dans la méthode
            Object[] parameters = getMethodParameters(method, request);

            // Créer une instance de l'objet contrôleur
            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

            // Exécuter la méthode avec les paramètres récupérés
            Object returnValue = method.invoke(controllerInstance, parameters);

            // Gérer le cas des API REST
            if (method.isAnnotationPresent(Restapi.class)) {
                // Réponse JSON pour les méthodes REST
                response.setContentType("application/json");
                Gson gson = new Gson();
                String jsonResponse;

                if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;
                    jsonResponse = gson.toJson(modelView.getData());
                } else {
                    jsonResponse = gson.toJson(returnValue);
                }

                out.print(jsonResponse);
            } else {
                // Pour les autres types de retour non REST
                if (returnValue instanceof String) {
                    out.println("Méthode trouvée dans " + returnValue);
                } else if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;

                    // Ajouter les données du modèle à la requête
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    // Faire suivre la requête au bon fichier JSP
                    RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                    dispatcher.forward(request, response);
                } else {
                    out.println("Type de données non reconnu");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Erreur lors du traitement de la requête : " + e.getMessage() + "</p>");
        }

        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public void scan() throws Exception {
        try {
            String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
            String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
            String packagePath = decodedPath + "\\" + controllerPackage.replace('.', '\\');
            File packageDirectory = new File(packagePath);
            if (!packageDirectory.exists() || !packageDirectory.isDirectory()) {
                throw new Exception("Package n'existe pas");
            } else {
                File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
                if (classFiles != null) {
                    for (File classFile : classFiles) {
                        String className = controllerPackage + '.'
                                + classFile.getName().substring(0, classFile.getName().length() - 6);
                        try {
                            Class<?> classe = Class.forName(className);
                            if (classe.isAnnotationPresent(AnnotationController.class)) {
                                controller.add(classe.getSimpleName());

                                Method[] methodes = classe.getDeclaredMethods();

                                for (Method methode : methodes) {
                                    if (methode.isAnnotationPresent(GetAnnotation.class)) {
                                        Mapping map = new Mapping(className, methode.getName(), "GET");
                                        String valeur = methode.getAnnotation(GetAnnotation.class).value();
                                        if (lien.containsKey(valeur + "-GET")) {
                                            throw new Exception("Double URL avec GET: " + valeur);
                                        } else {
                                            lien.put(valeur + "-GET", map);
                                        }
                                    } else if (methode.isAnnotationPresent(Post.class)) {
                                        Mapping map = new Mapping(className, methode.getName(), "POST");
                                        String valeur = methode.getAnnotation(Post.class).value();
                                        if (lien.containsKey(valeur + "-POST")) {
                                            throw new Exception("Double URL avec POST: " + valeur);
                                        } else {
                                            lien.put(valeur + "-POST", map);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw e;
                        }

                    }
                } else {
                    throw new Exception("le package est vide");
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private Object createRequestBodyParameter(Parameter parameter, Map<String, String[]> paramMap) throws Exception {
        Class<?> paramType = parameter.getType();
        Object paramObject = paramType.getDeclaredConstructor().newInstance();
        for (Field field : paramType.getDeclaredFields()) {
            String paramName = field.getName();
            if (paramMap.containsKey(paramName)) {
                String paramValue = paramMap.get(paramName)[0]; // Assuming single value for simplicity
                field.setAccessible(true);
                field.set(paramObject, paramValue);
            }
        }
        return paramObject;
    }
    
    private Object[] getMethodParameters(Method method, HttpServletRequest request) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];
    
        HttpSession session = request.getSession();
    
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType() == CustomSession.class) {
                parameterValues[i] = new CustomSession(session);
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                parameterValues[i] = createRequestBodyParameter(parameter, request.getParameterMap());
            } else if (parameter.isAnnotationPresent(Param.class)) {
                Param param = parameter.getAnnotation(Param.class);
                parameterValues[i] = request.getParameter(param.value()); // Assuming all parameters are strings for simplicity
            } else {
                throw new IllegalArgumentException("Paramètre non supporté pour cette méthode");
            }
        }
    
        return parameterValues;
    }
    
}

class Mapping {
    String className;
    String methodeName;
    String verb;  

    public Mapping(String className, String methodeName, String verb) {
        this.className = className;
        this.methodeName = methodeName;
        this.verb = verb; 
    }

    public String getClassName() {
        return className;
    }

    public String getMethodeName() {
        return methodeName;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }
}
