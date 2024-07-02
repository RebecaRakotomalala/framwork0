package mg.itu.prom16;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontController extends HttpServlet {
    private List<String> controllers = new ArrayList<>();
    private String controllerPackage;
    private boolean checked = false;
    private HashMap<String, Mapping> routes = new HashMap<>();
    private String error = "";

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        try {
            scanControllers();
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
            out.println("<p>Error: " + error + "</p>");
        } else if (!routes.containsKey(controllerSearched)) {
            out.println("<p>Méthode non trouvée.</p>");
        } else {
            try {
                Mapping mapping = routes.get(controllerSearched);
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method method = findMethodForRequest(clazz, mapping.getMethodName(), request.getMethod());

                if (method == null) {
                    out.println("<p>Aucune méthode correspondante trouvée.</p>");
                    return;
                }

                Object[] parameters = getMethodParameters(method, request);
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                Object returnValue = method.invoke(controllerInstance, parameters);

                handleReturnValue(returnValue, request, response, out);
            } catch (Exception e) {
                e.printStackTrace();
                out.println("<p>ETU002502 , Methode non annote.</p>");
            }
        }
        out.close();
    }

    private Method findMethodForRequest(Class<?> clazz, String methodName, String requestMethod) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (requestMethod.equalsIgnoreCase("GET") && method.isAnnotationPresent(GetAnnotation.class)) {
                    return method;
                } else if (requestMethod.equalsIgnoreCase("POST") && method.isAnnotationPresent(Post.class)) {
                    return method;
                }
            }
        }
        return null;
    }

    private void handleReturnValue(Object returnValue, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws ServletException, IOException {
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

    public void scanControllers() throws Exception {
        try {
            String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
            String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
            String packagePath = decodedPath + File.separator + controllerPackage.replace('.', File.separatorChar);
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
                            Class<?> clazz = Class.forName(className);
                            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                                controllers.add(clazz.getSimpleName());

                                Method[] methods = clazz.getDeclaredMethods();
                                for (Method method : methods) {
                                    if (method.isAnnotationPresent(GetAnnotation.class)) {
                                        addRoute(method, className, method.getAnnotation(GetAnnotation.class).value());
                                    } else if (method.isAnnotationPresent(Post.class)) {
                                        addRoute(method, className, method.getAnnotation(Post.class).value());
                                    }
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

    private void addRoute(Method method, String className, String url) throws Exception {
        Mapping map = new Mapping(className, method.getName());
        if (routes.containsKey(url)) {
            throw new Exception("URL déjà mappée: " + url);
        } else {
            routes.put(url, map);
        }
    }

    private Object[] getMethodParameters(Method method, HttpServletRequest request) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];

        Enumeration<String> params = request.getParameterNames();
        Map<String, String> paramMap = new HashMap<>();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            paramMap.put(paramName, request.getParameter(paramName));
        }

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType().equals(CustomSession.class)) {
                HttpSession httpSession = request.getSession();
                CustomSession customSession = (CustomSession) httpSession.getAttribute("customSession");
                if (customSession == null) {
                    customSession = new CustomSession();
                    httpSession.setAttribute("customSession", customSession);
                }
                parameterValues[i] = customSession;
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                parameterValues[i] = createRequestBodyParameter(parameter, paramMap);
            } else if (parameter.isAnnotationPresent(Param.class)) {
                Param param = parameter.getAnnotation(Param.class);
                parameterValues[i] = paramMap.get(param.value()); // Supposons que tous les paramètres sont des chaînes pour simplifier
            } else {
                throw new IllegalArgumentException("ETU002502 le paramètre " + parameter.getName() + " de la méthode " + method.getName() + " n'est pas annoté avec @RequestBody ou @GetParam");
            }
        }
        return parameterValues;
    }

    private Object createRequestBodyParameter(Parameter parameter, Map<String, String> paramMap) throws Exception {
        Class<?> paramType = parameter.getType();
        Object paramObject = paramType.getDeclaredConstructor().newInstance();
        for (Field field : paramType.getDeclaredFields()) {
            String paramName = field.getName();
            if (paramMap.containsKey(paramName)) {
                field.setAccessible(true);
                field.set(paramObject, paramMap.get(paramName)); // Vous pouvez ajouter une conversion de type ici si nécessaire
            }
        }
        return paramObject;
    }
}

class Mapping {
    private String className;
    private String methodName;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
