
    $( "#tables" ).autocomplete({
      source: tables
    });

    $( function() {
    function get_columns() {
        table = document.getElementById("tables").value;
        return tables[table];
    };
    function extractLast( term ) {
        if (term.indexOf("@")!=-1){
            var tmp=term.split("@");
            return tmp[tmp.length-1];
        }
        return null;
    };

    $( "#column_names" )
      .on( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB &&
            $( this ).autocomplete( "instance" ).menu.active ) {
          event.preventDefault();
        }
      })
      .autocomplete({
        minLength: 0,
        source: function( request, response ) {
            var column = extractLast(request.term);
            if(column==null){return;}
            response( $.ui.autocomplete.filter(
            get_columns(), column ) );
        },
        focus: function() {
          return false;
        },
        select: function( event, ui ) {
          var terms = this.value.split("@");
          terms.pop();
          terms.push( ui.item.value );
          terms.push( "" );
          this.value = terms.join( "" );
          return false;
        }
      });
  } );
