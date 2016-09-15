		</div>
		<div id="push"></div>
    </div>
    
    <div id="footer">
      <div class="container">
        <p class="muted credit">&copy; 2015 Jörg Prante <a href="http://creativecommons.org/licenses/by/4.0/"><img src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>images/CC-BY_icon.svg.png" width="88" height="31"></a>
        | Mixed with <a href="http://getbootstrap.com/">Bootstrap v3.3.1</a>
        | Baked with <a href="http://jbake.org">JBake ${version}</a></p>
      </div>
    </div>
    
    <!-- javascript -->
    <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/jquery-2.1.3.min.js"></script>
    <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/bootstrap.min.js"></script>
    <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/prettify.js"></script>

  </body>
</html>