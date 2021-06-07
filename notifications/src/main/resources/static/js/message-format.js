var simplemde = new SimpleMDE({
	blockStyles: {
    	bold: "*",
    	italic: "_",
    },
    element: document.getElementById("message-format"),
	placeholder: "Type message here...",
	toolbar: ["bold", "italic", "strikethrough", "code", "quote", "link", "|", "unordered-list", "ordered-list", "image", "preview",
	        {
    			name: "emoji",
    			action: function customFunction(editor){
                        // Add code for loading and displaying emojis
                },
    			className: "fa fa-star",
    			title: "Emoji",
    		}]
});