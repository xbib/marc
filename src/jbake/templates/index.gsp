<%include "header.gsp"%>

	<%include "menu.gsp"%>

	<div class="page-header">
		<h1>Index</h1>
	</div>
	<%published_posts.each {post ->%>
	    <div class="post">
		<a href="${post.uri}">${post.title}</a>
		<p class="post-info"><i class="glyphicon glyphicon-calendar"></i>&nbsp;${post.date.format("dd MMMM yyyy")}</p>
        </div>
  	<%}%>
	
	<hr />
	
	<p>Older posts are available in the <a href="/${config.archive_file}">archive</a>.</p>

<%include "footer.gsp"%>