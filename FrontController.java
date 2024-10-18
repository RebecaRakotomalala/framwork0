package mg.itu.prom16;

import mg.itu.prom16.AnnotationController;
import mg.itu.prom16.GetAnnotation;
import mg.itu.prom16.Post;
import mg.itu.prom16.Param;
import java.lang.reflect.Field;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    String[] requestUrlSplitted = request.getRequestURL().toString().split("/");
    String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

    response.setContentType("text/html");

    if (!error.isEmpty()) {
        out.println(error);
    } else if (!lien.containsKey(controllerSearched)) {
        // Lever une exception personnalisée pour une URL non trouvée
        throw new ServletException("Erreur 404 : L'URL " + controllerSearched + " n'existe pas.");
    } else {
        try {
            Mapping mapping = lien.get(controllerSearched);
            Class<?> clazz = Class.forName(mapping.getClassName());
            Method method = null;

            // Trouver l'action correspondant au verbe HTTP de la requête
            for (VerbAction action : mapping.getActions()) {
                if (action.getVerb().equalsIgnoreCase(request.getMethod())) {
                    method = clazz.getMethod(action.getMethodName());
                    break;
                }
            }

            if (method == null) {
                // Lever une exception si aucune méthode n'est trouvée pour le verbe HTTP
                throw new ServletException("Erreur 405 : Aucune méthode correspondante trouvée pour le verbe " + request.getMethod());
            }

            // Injecter les paramètres
            Object[] parameters = getMethodParameters(method, request);

            Object object = clazz.getDeclaredConstructor().newInstance();
            Object returnValue = method.invoke(object, parameters);

            // Vérifier l'annotation Restapi
            if (method.isAnnotationPresent(Restapi.class)) {
                // Convertir en JSON et envoyer la réponse
                response.setContentType("application/json");
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = "";

                if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;
                    jsonResponse = objectMapper.writeValueAsString(modelView.getData());
                } else {
                    jsonResponse = objectMapper.writeValueAsString(returnValue);
                }

                out.println(jsonResponse);
            } else {
                // Continuer le traitement si Restapi n'est pas présent
                if (returnValue instanceof String) {
                    out.println("Méthode trouvée dans " + returnValue);
                } else if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                    dispatcher.forward(request, response);
                } else {
                    out.println("Type de données non reconnu");
                }
            }
        } catch (ServletException e) {
            // Afficher le message d'erreur (404, 405, etc.)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<p>" + e.getMessage() + "</p>");
        } catch (Exception e) {
            // Gérer les erreurs générales
            e.printStackTrace();
        }
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

                                Mapping map = new Mapping(className);
                                Method[] methodes = classe.getDeclaredMethods();

                                for (Method methode : methodes) {
                                    if (methode.isAnnotationPresent(GetAnnotation.class)) {
                                        VerbAction getAction = new VerbAction("GET", methode.getName());
                                        map.addAction(getAction);
                                    } else if (methode.isAnnotationPresent(Post.class)) {
                                        VerbAction postAction = new VerbAction("POST", methode.getName());
                                        map.addAction(postAction);
                                    }
                                }

                                String valeur = methodes[0].isAnnotationPresent(GetAnnotation.class) ?
                                        methodes[0].getAnnotation(GetAnnotation.class).value() :
                                        methodes[0].getAnnotation(Post.class).value();

                                if (lien.containsKey(valeur)) {
                                    throw new Exception("double url " + valeur);
                                } else {
                                    lien.put(valeur, map);
                                }
                            }
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                } else {
                    throw new Exception("Le package est vide");
                }
            }
        } catch (Exception e) {
            throw e;
        }
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
                parameterValues[i] = request.getParameter(param.value());
            } else {
                throw new IllegalArgumentException("Paramètre non supporté pour cette méthode");
            }
        }

        return parameterValues;
    }

    private Object createRequestBodyParameter(Parameter parameter, Map<String, String[]> paramMap) throws Exception {
        Class<?> paramType = parameter.getType();
        Object paramObject = paramType.getDeclaredConstructor().newInstance();
        for (Field field : paramType.getDeclaredFields()) {
            String paramName = field.getName();
            if (paramMap.containsKey(paramName)) {
                String paramValue = paramMap.get(paramName)[0];
                field.setAccessible(true);
                field.set(paramObject, paramValue);
            }
        }
        return paramObject;
    }
}

class Mapping {
    private String methodName;
    private List<VerbAction> verbActions; 

    public Mapping(String className) {
        this.methodName = className;
        this.verbActions = new ArrayList<>(); 
    }

    public void setClassName(String className) {
        this.methodName = className;
    }

    public String getClassName() {
        return this.methodName;  // Assuming methodName holds the class name
    }    

    public List<VerbAction> getActions() {
        return verbActions;
    }  
    
    public String getMethodName() {
        return methodName;  // Assuming methodName is the field in VerbAction holding the method name
    }    

    public void addAction(VerbAction action) {
        verbActions.add(action);
    }    

    public VerbAction getVerbActionByVerb(String verb) {
        for (VerbAction action : verbActions) {
            if (action.getVerb().equalsIgnoreCase(verb)) {
                return action;
            }
        }
        return null;  
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + methodName + '\'' +
                ", verbActions=" + verbActions +
                '}';
    }
}  