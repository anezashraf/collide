package collide.demo.parent;

import collide.client.util.Elements;
import collide.demo.controller.DemoController;
import collide.demo.view.DemoView;
import collide.gwtc.ui.GwtCompilerService;
import collide.gwtc.ui.GwtCompilerShell;
import collide.gwtc.ui.GwtCompilerShell.Resources;
import collide.plugin.client.launcher.LauncherService;
import collide.plugin.client.terminal.TerminalService;
import com.google.collide.client.AppContext;
import com.google.collide.client.CollideBootstrap;
import com.google.collide.client.communication.FrontendApi.ApiCallback;
import com.google.collide.client.history.Place;
import com.google.collide.client.plugin.ClientPlugin;
import com.google.collide.client.plugin.ClientPluginService;
import com.google.collide.client.plugin.FileAssociation;
import com.google.collide.client.plugin.RunConfiguration;
import com.google.collide.client.status.StatusManager;
import com.google.collide.client.status.StatusMessage;
import com.google.collide.client.ui.button.ImageButton;
import com.google.collide.client.ui.panel.MultiPanel;
import com.google.collide.client.workspace.Header;
import com.google.collide.client.workspace.WorkspacePlace;
import com.google.collide.clientlibs.model.Workspace;
import com.google.collide.dto.GwtRecompile;
import com.google.collide.dto.GwtSettings;
import com.google.collide.dto.ServerError.FailureReason;
import com.google.collide.shared.plugin.PublicService;
import com.google.collide.shared.plugin.PublicServices;
import com.google.gwt.resources.client.ImageResource;
import elemental.client.Browser;
import elemental.html.ScriptElement;
import xapi.log.X_Log;
import xapi.util.api.SuccessHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

/**
 * This demo class shows how to use gwt reflection to send
 * dtos across different compilations of gwt,
 * as if the objects were merely loaded by a different classloader.
 *
 * @author "James X. Nelson (james@wetheinter.net)"
 *
 */
public class ParentModule implements EntryPoint{


  @Override
  public void onModuleLoad() {

    // First, let's setup a callback for child modules to talk to us
    setupNatives();

    // Now, let's bootstrap collide
    startCollide();
  }

  private void startCollide() {
    GWT.runAsync(AppContext.class, new RunAsyncCallback() {
      @Override
      public void onSuccess() {
        CollideBootstrap.start(new SuccessHandler<AppContext>() {
          @Override
          public void onSuccess(AppContext context) {
            DemoView body = new DemoView();
            ClientPluginService plugins = ClientPluginService.initialize
                (context, body, WorkspacePlace.PLACE);
            addGwtCompiler(context, body);
            addEditor(context, body, plugins);
            addTerminal(context, body);
          }

        });
      }

      @Override
      public void onFailure(Throwable reason) {
        warn(reason, "Loading Collide's AppContext ");
      }
    });
  }

  private void addTerminal(final AppContext context, DemoView body) {
    //GWT.runAsync(TerminalService.class, new RunAsyncCallback() {
    //  @Override
    //  public void onSuccess() {
    //
    //  }
    //
    //  @Override
    //  public void onFailure(Throwable reason) {
    //    warn(reason, "Loading Collide logger ");
    //  }
    //});
  }

  private void addEditor(final AppContext context, final DemoView body, final ClientPluginService plugins) {
    //GWT.runAsync(DemoController.class, new RunAsyncCallback() {
    //  @Override
    //  public void onSuccess() {
        new DemoController(context).initialize(body, plugins);
    //  }
    //
    //  @Override
    //  public void onFailure(Throwable reason) {
    //    warn(reason, "Loading Collide workspace ");
    //  }
    //});
  }

  private void addGwtCompiler(final AppContext context, final DemoView body) {
    //GWT.runAsync(GwtRecompile.class, new RunAsyncCallback() {
    //  @Override
    //  public void onSuccess() {
        // Inject css
        Resources res = GWT.create(Resources.class);
        res.gwtCompilerCss().ensureInjected();
        res.gwtLogCss().ensureInjected();
        res.gwtClasspathCss().ensureInjected();
        res.gwtModuleCss().ensureInjected();

        // Prepare view
        GwtCompilerService gwtCompiler = PublicServices.getService(GwtCompilerService.class);

        final GwtCompilerShell gwt = gwtCompiler.getShell();
        // Attach compiler
        body.initGwt(gwt);

        ResizeHandler handler = e -> {
            Elements.getBody().getStyle().setWidth(Browser.getWindow().getInnerWidth()+"px");
            Elements.getBody().getStyle().setHeight(Browser.getWindow().getInnerHeight()+"px");
        };
        Elements.getBody().getStyle().setPosition("absolute");

        Window.addResizeHandler(handler);
        handler.onResize(null);

        // Request module configs
        context.getFrontendApi().GWT_SETTINGS.request(
          new ApiCallback<GwtSettings>() {
            @Override
            public void onFail(FailureReason reason) {
              warn(context, "Retrieving gwt settings");
            }
            @Override
            public void onMessageReceived(GwtSettings response) {
              gwt.showResults(response);
              GwtRecompile defaultModule = response.getModules().get(0);

            }
          }
        );
    //  }
    //
    //  @Override
    //  public void onFailure(Throwable reason) {
    //    warn(reason, "Loading GWT compiler ");
    //  }
    //});
  }

  private native void setupNatives()
  /*-{
    $wnd.Callback = function(cls, obj) {
      @collide.demo.parent.ParentModule::receiveForeign(*)(cls, obj);
    };
  }-*/;

  public static void receiveForeign(Object cls, Object obj) {
    X_Log.info("Received foreign object",cls, obj);
  }

  private void injectForeignModulue(String src) {
    ScriptElement script = Browser.getDocument().createScriptElement();
    script.setSrc(src);
    Browser.getDocument().getHead().appendChild(script);
  }
  private void warn(Throwable e, String warning) {
    warn((AppContext)null, e+"\n"+warning);
    e.printStackTrace();
  }
  private void warn(AppContext ctx, String warning) {
    StatusManager manager = ctx == null ? new StatusManager() : ctx.getStatusManager();
    new StatusMessage(manager, StatusMessage.MessageType.ERROR,
        warning+" failed in 3 attempts.  Try again later.").fire();
  }

}
