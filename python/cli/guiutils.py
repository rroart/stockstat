import os
import tkinter as tk

def view(output):
    display = os.environ.get('DISPLAY')
    if display is None:
        return
    root = tk.Tk()
    txt = tk.Text(master = root, fg='green', bg='black')
    txt.pack(side=tk.LEFT)
    txt.insert(tk.END, output)
    scrollbar = tk.Scrollbar(root)
    scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
    txt.config(yscrollcommand=scrollbar.set)
    scrollbar.config(command=txt.yview)
    tk.mainloop()
    
def grid(list):
    root = tk.Tk()
    for i in range(len(list)):
        for j in range(len(list[i])):
            entry = tk.Entry(root, text="")
            entry.grid(row=i, column=j)
            entry.insert(tk.END, list[i][j])
            
    #scrollbarx = tk.Scrollbar(root)
    #scrollbarx.pack(side=tk.BOTTOM, fill=tk.X)
    #root.config(xscrollcommand=scrollbarx.set)
    #scrollbarx.config(command=root.xview)

    #scrollbary = tk.Scrollbar(root)
    #scrollbary.pack(side=tk.RIGHT, fill=tk.Y)
    #root.config(yscrollcommand=scrollbary.set)
    #scrollbary.config(command=root.yview)
    
    tk.mainloop()
                         
def gridnew(list):
    root = tk.Tk()
    root.grid_rowconfigure(0, weight=1)
    root.columnconfigure(0, weight=1)
    frame_main = tk.Frame(root, bg="gray")
    frame_main.grid(sticky='news')
    frame_canvas = tk.Frame(frame_main)
    frame_canvas.grid(row=2, column=0, pady=(5, 0), sticky='nw')
    frame_canvas.grid_rowconfigure(0, weight=1)
    frame_canvas.grid_columnconfigure(0, weight=1)
    # Set grid_propagate to False to allow 5-by-5 buttons resizing later
    frame_canvas.grid_propagate(False)
    canvas = tk.Canvas(frame_canvas, bg="yellow")
    #canvas.grid(row=0, column=0, sticky="news")
    # Link a scrollbar to the canvas
    vsb = tk.Scrollbar(frame_canvas, orient="vertical", command=canvas.yview)
    vsb.grid(row=0, column=1, sticky='ns')
    canvas.configure(yscrollcommand=vsb.set)

    frame_canvas.config(width = 500, height = 50)
    canvas.config(scrollregion=canvas.bbox("all"))
    
    for i in range(len(list)):
        for j in range(len(list[i])):
            entry = tk.Entry(frame_canvas, text="")
            entry.grid(row=i, column=j)
            entry.insert(tk.END, list[i][j])
            
    root.mainloop()
                         
