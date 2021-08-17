
    $( function() {
    function get_columns() {
        var separators = [' join ', ' inner join ', ' left join ', ' right join '];
        var table = document.getElementById("table_names").value.toLowerCase().split(new RegExp(separators.join('|'), 'g'));
        var columns_joined = [];
        table.forEach(joinTables);
        function joinTables(item, index, arr) {
        tables[item].forEach((column) => {
                  column.table = item;
                  console.log(column);
                  });

        columns_joined = columns_joined.concat(tables[item]);
        };

        console.log(columns_joined);
        return [...new Set(columns_joined)];
    };
    function extractLast( term ) {
        if (term.indexOf("@")!=-1){
            var tmp=term.split("@");
            return tmp[tmp.length-1];
        }
        return null;
    };

    $( "#table_names" )
            .on( "keydown", function( event ) {
                if ( event.keyCode === $.ui.keyCode.TAB &&
                    $( this ).autocomplete( "instance" ).menu.active ) {
                  event.preventDefault();
                }
              })
              .autocomplete({
                minLength: 0,
                source: function( request, response ) {
                    var table = extractLast(request.term);
                    if(table==null){return;}
                    response( $.ui.autocomplete.filter(
                    table_names, table ) );
                },
                focus: function() {
                  return false;
                },
                select: function( event, ui ) {
                  var terms = this.value.split("@");
                  terms.pop();
                  terms.push( ui.item.label );
                  terms.push( "" );
                  this.value = terms.join( "" );
                  return false;
                }
              });

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
          terms.push( ui.item.table + '.' + ui.item.label );
          terms.push( "" );
          this.value = terms.join( "" );
          return false;
        }
      })
      .autocomplete( "instance" )._renderItem = function( ul, item ) {
            return $( "<li>" )
              .append( "<div><b>" + item.table + '.' + item.label + "</b> <i>" + item.value + "</i></div>" )
              .appendTo( ul );
          };
  } );
