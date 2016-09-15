<%include "header.gsp"%>
	
	<%include "menu.gsp"%>

	<div class="post">

	<div class="page-header">
		<h1>${content.title}</h1>
	</div>

    <p class="post-info">
		<i class="fa fa-calendar-o"></i>
		&nbsp;${content.date.format("dd MMMM yyyy")}
	</p>

	<p>${content.body}</p>

    </div>

<%include "footer.gsp"%>